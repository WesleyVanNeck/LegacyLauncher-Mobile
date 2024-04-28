package net.kdt.pojavlaunch.multirt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.kdt.pojavlaunch.R;

import java.util.List;

public class RTSpinnerAdapter implements SpinnerAdapter {
    final Context mContext;
    final List<Runtime> mRuntimes;

    public RTSpinnerAdapter(@NonNull Context context, @NonNull List<Runtime> runtimes) {
        mRuntimes = runtimes;
        Runtime defaultRuntime = new Runtime("<Default>", "", null, 0);
        mRuntimes.add(defaultRuntime);
        mContext = context;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public int getCount() {
        return mRuntimes.size();
    }

    @Override
    public Runtime getItem(int position) {
        return mRuntimes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mRuntimes.get(position).name.hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_simple_list_1, parent, false);
        } else {
            view = convertView;
        }
        view.setTag(position);

        Runtime runtime = mRuntimes.get(position);
        TextView textView = view.findViewById(R.id.textView);
        if (position == mRuntimes.size() - 1) {
            textView.setText(runtime.name);
        } else {
            textView.setText(String.format("%s - %s",
                    runtime.name.replace(".tar.xz", ""),
                    runtime.versionString == null ? parent.getResources().getString(R.string.multirt_runtime_corrupt) : runtime.versionString));
        }

        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;

