package net.kdt.pojavlaunch.modloaders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.TextView;

import java.util.List;

public class OptiFineVersionListAdapter extends BaseExpandableListAdapter implements ExpandableListAdapter {

    private final LayoutInflater layoutInflater;
    private final List<String> minecraftVersions;
    private final List<List<OptiFineUtils.OptiFineVersion>> optifineVersions;

    public OptiFineVersionListAdapter(LayoutInflater layoutInflater, List<String> minecraftVersions, List<List<OptiFineUtils.OptiFineVersion>> optifineVersions) {
        this.layoutInflater = layoutInflater;
        this.minecraftVersions = minecraftVersions;
        this.optifineVersions = optifineVersions;
    }

    @Override
    public int getGroupCount() {
        return minecraftVersions.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return optifineVersions.get(i).size();
    }

    @Override
    public Object getGroup(int i) {
        return minecraftVersions.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return optifineVersions.get(i).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int i, boolean b, View convertView, ViewGroup viewGroup) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(android.R.layout.simple_expandable_list_item_1, viewGroup, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText((String) getGroup(i));

        return convertView;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View convertView, ViewGroup viewGroup) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(android.R.layout.simple_expandable_list_item_1, viewGroup, false);
        }

        OptiFineUtils.OptiFineVersion optiFineVersion = (OptiFineUtils.OptiFineVersion) getChild(i, i1);
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(optiFineVersion.versionName);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
