package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.VinogramDeletedArchive;
import org.telegram.messenger.VinogramMod;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.Switch;

import java.util.ArrayList;

/**
 * The vinogram-only settings: ghost mode, deleted messages and the download
 * override. Opened from the "Mod features" row in the main settings.
 */
public class VinogramModActivity extends BaseFragment {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_SWITCH = 1;
    private static final int TYPE_INFO = 2;
    private static final int TYPE_BUTTON = 3;

    private final ArrayList<Row> rows = new ArrayList<>();
    private RecyclerListView listView;
    private ListAdapter adapter;

    private interface Getter {
        boolean get();
    }

    private interface Setter {
        void set(boolean value);
    }

    private interface Value {
        CharSequence get();
    }

    private static class Row {
        final int type;
        final CharSequence text;
        Getter getter;
        Setter setter;
        Value value;
        Runnable action;

        Row(int type, CharSequence text) {
            this.type = type;
            this.text = text;
        }
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString(R.string.VinogramMod));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        if (parentLayout != null && parentLayout.isRightLayout()) {
            actionBar.setBackButtonImage(R.drawable.ic_ab_close);
        }

        fillRows();

        final FrameLayout contentView = new FrameLayout(context);
        contentView.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));

        listView = new RecyclerListView(context);
        listView.setSections(true);
        actionBar.setAdaptiveBackground(listView);
        listView.setLayoutManager(new LinearLayoutManager(context));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(adapter = new ListAdapter(context));
        listView.setOnItemClickListener((view, position, x, y) -> {
            if (position < 0 || position >= rows.size()) {
                return;
            }
            final Row row = rows.get(position);
            if (row.type == TYPE_SWITCH && row.getter != null && row.setter != null) {
                row.setter.set(!row.getter.get());
                VinogramMod.saveConfig();
                refresh();
            } else if (row.type == TYPE_BUTTON && row.action != null) {
                row.action.run();
            }
        });
        contentView.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        fragmentView = contentView;
        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // The archive count can change while a chat is open on top of us.
        if (adapter != null) {
            refresh();
        }
    }

    private void refresh() {
        fillRows();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void fillRows() {
        rows.clear();

        rows.add(new Row(TYPE_HEADER, LocaleController.getString(R.string.VinogramModGhost)));
        addSwitch(R.string.VinogramModGhostOnline,
                () -> VinogramMod.ghostHideOnline,
                value -> VinogramMod.ghostHideOnline = value);
        addSwitch(R.string.VinogramModGhostTyping,
                () -> VinogramMod.ghostHideTyping,
                value -> VinogramMod.ghostHideTyping = value);
        addSwitch(R.string.VinogramModGhostRead,
                () -> VinogramMod.ghostHideRead,
                value -> VinogramMod.ghostHideRead = value);
        rows.add(new Row(TYPE_INFO, LocaleController.getString(R.string.VinogramModGhostInfo)));

        rows.add(new Row(TYPE_HEADER, LocaleController.getString(R.string.VinogramModMessages)));
        addSwitch(R.string.VinogramModDeleted,
                () -> VinogramMod.keepDeletedMessages,
                value -> VinogramMod.keepDeletedMessages = value);
        if (VinogramMod.keepDeletedMessages) {
            final Row archive = new Row(TYPE_BUTTON, LocaleController.getString(R.string.VinogramModDeletedArchive));
            archive.value = () -> String.valueOf(VinogramDeletedArchive.getCount());
            archive.action = () -> presentFragment(new VinogramDeletedActivity());
            rows.add(archive);
        }
        rows.add(new Row(TYPE_INFO, LocaleController.getString(R.string.VinogramModDeletedInfo)));

        rows.add(new Row(TYPE_HEADER, LocaleController.getString(R.string.VinogramModDownloads)));
        addSwitch(R.string.VinogramModUnlimited,
                () -> VinogramMod.unlimitedDownloads,
                value -> VinogramMod.unlimitedDownloads = value);
        rows.add(new Row(TYPE_INFO, LocaleController.getString(R.string.VinogramModUnlimitedInfo)));
    }

    private void addSwitch(int textRes, Getter getter, Setter setter) {
        final Row row = new Row(TYPE_SWITCH, LocaleController.getString(textRes));
        row.getter = getter;
        row.setter = setter;
        rows.add(row);
    }

    private class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final Context context;

        private ListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }

        @Override
        public int getItemViewType(int position) {
            return rows.get(position).type;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final View view;
            if (viewType == TYPE_HEADER) {
                view = new HeaderCell(context);
            } else if (viewType == TYPE_SWITCH) {
                view = new SwitchRow(context);
            } else if (viewType == TYPE_BUTTON) {
                view = new TextCell(context);
            } else {
                view = new TextInfoPrivacyCell(context);
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
            ));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final Row row = rows.get(position);
            if (row.type == TYPE_HEADER) {
                ((HeaderCell) holder.itemView).setText(row.text);
            } else if (row.type == TYPE_SWITCH) {
                final SwitchRow cell = (SwitchRow) holder.itemView;
                cell.setText(row.text);
                cell.setChecked(row.getter != null && row.getter.get());
            } else if (row.type == TYPE_BUTTON) {
                final TextCell cell = (TextCell) holder.itemView;
                cell.setTextAndValue(row.text, row.value == null ? "" : row.value.get(), true);
            } else {
                final TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                cell.setText(row.text);
                cell.setBackground(null);
            }
        }
    }

    private class SwitchRow extends FrameLayout {

        private final TextView textView;
        private final Switch switchView;

        private SwitchRow(Context context) {
            super(context);

            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);

            textView = new TextView(context);
            textView.setLines(1);
            textView.setSingleLine(true);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            textView.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
            textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
            textView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT,
                    (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL, 21, 0, 76, 0));

            switchView = new Switch(context);
            switchView.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_windowBackgroundWhite, Theme.key_windowBackgroundWhite);
            switchView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            addView(switchView, LayoutHelper.createFrame(37, 40,
                    Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT), 19, 0, 19, 0));

            setFocusable(true);
        }

        private void setText(CharSequence text) {
            textView.setText(text);
        }

        private void setChecked(boolean checked) {
            switchView.setChecked(checked, false);
            setContentDescription(textView.getText() + ", " +
                    LocaleController.getString(checked ? R.string.VinogramModOn : R.string.VinogramModOff));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(dp(50), MeasureSpec.EXACTLY)
            );
        }
    }
}
