package by.instruction.planer;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class PlannerWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new PlannerWidgetFactory(this.getApplicationContext(), intent);
    }
}