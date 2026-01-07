package by.instruction.planer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FolderActivity extends AppCompatActivity {

    public static final String EXTRA_SECTION_KEY = "extra_section_key";
    public static final String EXTRA_SECTION_TITLE = "extra_section_title";
    public static final String EXTRA_DEFAULT_CHILDREN = "extra_default_children"; // comma separated

    private FolderStorage storage;
    private FolderAdapter adapter;
    private String sectionKey;
    private String sectionTitle;
    private String[] defaultChildren;
    private List<FolderStorage.FolderNode> flatData = new ArrayList<>();
    private NoteStorage noteStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folders);

        sectionKey = getIntent().getStringExtra(EXTRA_SECTION_KEY);
        sectionTitle = getIntent().getStringExtra(EXTRA_SECTION_TITLE);
        String defaults = getIntent().getStringExtra(EXTRA_DEFAULT_CHILDREN);
        defaultChildren = defaults == null ? new String[]{} : defaults.split(",");

        if (sectionTitle != null) {
            setTitle(sectionTitle);
        }

        storage = new FolderStorage(this);
        noteStorage = new NoteStorage(this);

        RecyclerView recyclerView = findViewById(R.id.folder_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FolderAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnItemActionListener(this::showItemActions);
        adapter.setOnItemClickListener(this::openNotes);

        findViewById(R.id.add_folder_fab).setOnClickListener(v -> showAddDialog());
        findViewById(R.id.add_note_fab).setOnClickListener(v -> openNotes(null));

        loadData();
    }

    private void loadData() {
        List<FolderStorage.FolderNode> nodes = storage.load(sectionKey);
        if (nodes.isEmpty() && defaultChildren.length > 0) {
            List<FolderStorage.FolderNode> seeded = new ArrayList<>();
            for (String name : defaultChildren) {
                String trimmed = name.trim();
                if (trimmed.isEmpty()) continue;
                seeded.add(new FolderStorage.FolderNode(storage.newId(), null, trimmed));
            }
            storage.save(sectionKey, seeded);
            nodes = seeded;
        }
        flatData = nodes;
        adapter.setData(buildTree(nodes));
    }

    private void saveData() {
        storage.save(sectionKey, adapter.getFlat());
        flatData = adapter.getFlat();
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_folder, null, false);
        EditText nameInput = dialogView.findViewById(R.id.folder_name_input);
        List<FolderStorage.FolderNode> flat = adapter.getFlat();

        List<String> titles = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        titles.add(getString(R.string.folder_root));
        ids.add(null);
        for (FolderStorage.FolderNode n : flat) {
            titles.add(n.name);
            ids.add(n.id);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.folder_add_title);
        builder.setView(dialogView);
        builder.setSingleChoiceItems(titles.toArray(new String[0]), 0, null);
        builder.setPositiveButton(R.string.folder_add_action, (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, R.string.folder_empty_name, Toast.LENGTH_SHORT).show();
                return;
            }
            AlertDialog dlg = (AlertDialog) dialog;
            ListView lv = dlg.getListView();
            int checked = lv.getCheckedItemPosition();
            String parentId = ids.get(checked);
            adapter.addNode(new FolderStorage.FolderNode(storage.newId(), parentId, name));
            saveData();
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private void showItemActions(FolderStorage.FolderNode node) {
        String[] actions = {getString(R.string.folder_rename), getString(R.string.folder_delete)};
        new AlertDialog.Builder(this)
                .setTitle(node.name)
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        showRenameDialog(node);
                    } else if (which == 1) {
                        deleteNode(node.id);
                    }
                })
                .show();
    }

    private void showRenameDialog(FolderStorage.FolderNode node) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_folder, null, false);
        EditText nameInput = dialogView.findViewById(R.id.folder_name_input);
        nameInput.setText(node.name);
        new AlertDialog.Builder(this)
                .setTitle(R.string.folder_rename)
                .setView(dialogView)
                .setPositiveButton(R.string.folder_rename, (d, w) -> {
                    String newName = nameInput.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(this, R.string.folder_empty_name, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<FolderStorage.FolderNode> updated = new ArrayList<>();
                    for (FolderStorage.FolderNode n : flatData) {
                        if (n.id.equals(node.id)) {
                            updated.add(new FolderStorage.FolderNode(n.id, n.parentId, newName));
                        } else {
                            updated.add(n);
                        }
                    }
                    storage.save(sectionKey, updated);
                    loadData();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void deleteNode(String nodeId) {
        List<FolderStorage.FolderNode> remaining = new ArrayList<>();
        Set<String> removedIds = new HashSet<>();
        for (FolderStorage.FolderNode n : flatData) {
            if (n.id.equals(nodeId) || isDescendant(nodeId, n)) continue;
            remaining.add(n);
        }
        for (FolderStorage.FolderNode n : flatData) {
            if (n.id.equals(nodeId) || isDescendant(nodeId, n)) {
                removedIds.add(n.id);
            }
        }
        storage.save(sectionKey, remaining);
        if (noteStorage != null && !removedIds.isEmpty()) {
            noteStorage.deleteByFolderIds(sectionKey, removedIds);
        }
        loadData();
    }

    private boolean isDescendant(String ancestorId, FolderStorage.FolderNode node) {
        String parent = node.parentId;
        while (parent != null) {
            if (parent.equals(ancestorId)) return true;
            parent = findParentId(parent);
        }
        return false;
    }

    private String findParentId(String id) {
        for (FolderStorage.FolderNode n : flatData) {
            if (n.id.equals(id)) return n.parentId;
        }
        return null;
    }

    private List<FolderAdapter.TreeItem> buildTree(List<FolderStorage.FolderNode> nodes) {
        Map<String, List<FolderStorage.FolderNode>> childrenMap = new HashMap<>();
        Map<String, FolderStorage.FolderNode> byId = new HashMap<>();
        for (FolderStorage.FolderNode n : nodes) {
            byId.put(n.id, n);
            String parent = n.parentId == null ? "" : n.parentId;
            if (!childrenMap.containsKey(parent)) childrenMap.put(parent, new ArrayList<>());
            childrenMap.get(parent).add(n);
        }
        List<FolderAdapter.TreeItem> result = new ArrayList<>();
        traverse("", 0, childrenMap, result);
        return result;
    }

    private void traverse(String parentId, int level, Map<String, List<FolderStorage.FolderNode>> map, List<FolderAdapter.TreeItem> out) {
        List<FolderStorage.FolderNode> list = map.get(parentId);
        if (list == null) return;
        for (FolderStorage.FolderNode n : list) {
            out.add(new FolderAdapter.TreeItem(n, level));
            traverse(n.id, level + 1, map, out);
        }
    }

    private void openNotes(FolderStorage.FolderNode node) {
        Intent intent = new Intent(this, NotesActivity.class);
        intent.putExtra(NotesActivity.EXTRA_SECTION_KEY, sectionKey);
        if (node != null) {
            intent.putExtra(NotesActivity.EXTRA_FOLDER_ID, node.id);
            intent.putExtra(NotesActivity.EXTRA_FOLDER_TITLE, node.name);
        } else if (sectionTitle != null) {
            intent.putExtra(NotesActivity.EXTRA_FOLDER_TITLE, sectionTitle);
        }
        startActivity(intent);
    }
}

