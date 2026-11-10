package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
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

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.VinogramDeletedArchive;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Shows the messages that the other side deleted while "view deleted messages"
 * was on. Tapping a row opens the chat the message belonged to.
 */
public class VinogramDeletedActivity extends BaseFragment {

    private static final int MENU_CLEAR = 1;
    private static final String META_SEPARATOR = " \u2022 ";

    private RecyclerListView listView;
    private ListAdapter adapter;
    private final ArrayList<VinogramDeletedArchive.Entry> entries = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM, HH:mm", Locale.getDefault());

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString(R.string.VinogramModDeleted));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == MENU_CLEAR) {
                    VinogramDeletedArchive.clear();
                    loadEntries();
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
        if (parentLayout != null && parentLayout.isRightLayout()) {
            actionBar.setBackButtonImage(R.drawable.ic_ab_close);
        }
        actionBar.createMenu().addItem(MENU_CLEAR, R.drawable.msg_delete);

        loadEntries();

        final FrameLayout contentView = new FrameLayout(context);
        contentView.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));

        listView = new RecyclerListView(context);
        listView.setSections(true);
        actionBar.setAdaptiveBackground(listView);
        listView.setLayoutManager(new LinearLayoutManager(context));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(adapter = new ListAdapter(context));
        listView.setOnItemClickListener((view, position, x, y) -> {
            if (position < 0 || position >= entries.size()) {
                return;
            }
            final VinogramDeletedArchive.Entry entry = entries.get(position);
            if (entry.dialogId == 0) {
                return;
            }
            final Bundle args = new Bundle();
            args.putLong("dialog_id", entry.dialogId);
            presentFragment(new ChatActivity(args));
        });
        contentView.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        fragmentView = contentView;
        return fragmentView;
    }

    private void loadEntries() {
        entries.clear();
        entries.addAll(VinogramDeletedArchive.getEntries());
        if (actionBar != null) {
            final View clear = actionBar.createMenu().getItem(MENU_CLEAR);
            if (clear != null) {
                clear.setVisibility(entries.isEmpty() ? View.GONE : View.VISIBLE);
            }
        }
    }

    private CharSequence dialogTitle(long dialogId) {
        if (dialogId > 0) {
            final TLRPC.User user = getMessagesController().getUser(dialogId);
            if (user != null) {
                return UserObject.getUserName(user);
            }
        } else if (dialogId < 0) {
            final TLRPC.Chat chat = getMessagesController().getChat(-dialogId);
            if (chat != null && !TextUtils.isEmpty(chat.title)) {
                return chat.title;
            }
        }
        return LocaleController.getString(R.string.VinogramModDeletedUnknownChat);
    }

    private class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final Context context;

        private ListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getItemCount() {
            return entries.isEmpty() ? 1 : entries.size();
        }

        @Override
        public int getItemViewType(int position) {
            return entries.isEmpty() ? 1 : 0;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final View view = viewType == 1 ? new TextInfoPrivacyCell(context) : new RowCell(context);
            view.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
            ));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (entries.isEmpty()) {
                final TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                cell.setText(LocaleController.getString(R.string.VinogramModDeletedEmpty));
                cell.setBackground(null);
                return;
            }
            final VinogramDeletedArchive.Entry entry = entries.get(position);
            final RowCell cell = (RowCell) holder.itemView;
            final StringBuilder meta = new StringBuilder();
            if (!TextUtils.isEmpty(entry.fromName)) {
                meta.append(entry.fromName).append(META_SEPARATOR);
            }
            meta.append(dateFormat.format(new Date(entry.date * 1000L)));
            if (entry.out) {
                meta.append(META_SEPARATOR).append(LocaleController.getString(R.string.VinogramModDeletedOutgoing));
            }
            cell.set(dialogTitle(entry.dialogId), meta.toString(),
                    TextUtils.isEmpty(entry.text)
                            ? LocaleController.getString(R.string.VinogramModDeletedMedia)
                            : entry.text,
                    position != entries.size() - 1);
        }
    }

    private class RowCell extends FrameLayout {

        private final TextView titleView;
        private final TextView metaView;
        private final TextView textView;
        private boolean needDivider;

        private RowCell(Context context) {
            super(context);

            final int gravity = (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP;

            titleView = new TextView(context);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            titleView.setTypeface(AndroidUtilities.bold());
            titleView.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
            titleView.setSingleLine(true);
            titleView.setEllipsize(TextUtils.TruncateAt.END);
            titleView.setGravity(gravity);
            addView(titleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, gravity, 21, 10, 21, 0));

            metaView = new TextView(context);
            metaView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            metaView.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteGrayText));
            metaView.setSingleLine(true);
            metaView.setEllipsize(TextUtils.TruncateAt.END);
            metaView.setGravity(gravity);
            addView(metaView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, gravity, 21, 30, 21, 0));

            textView = new TextView(context);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            textView.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
            textView.setMaxLines(4);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setGravity(gravity);
            textView.setLineSpacing(dp(2), 1f);
            addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, gravity, 21, 50, 21, 10));
        }

        private void set(CharSequence title, CharSequence meta, CharSequence text, boolean divider) {
            titleView.setText(title);
            metaView.setText(meta);
            textView.setText(text);
            needDivider = divider;
            setWillNotDraw(!divider);
            setContentDescription(title + ", " + meta + ", " + text);
            requestLayout();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (needDivider) {
                final float y = getMeasuredHeight() - 1;
                if (LocaleController.isRTL) {
                    canvas.drawLine(0, y, getMeasuredWidth() - dp(21), y, Theme.dividerPaint);
                } else {
                    canvas.drawLine(dp(21), y, getMeasuredWidth(), y, Theme.dividerPaint);
                }
            }
        }
    }
}
