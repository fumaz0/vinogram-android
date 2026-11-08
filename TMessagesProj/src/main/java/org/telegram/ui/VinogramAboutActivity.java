package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

/**
 * The vinogram about screen: logo, version, short description and the links to
 * the project. Opened from the brand card at the very top of the settings.
 */
public class VinogramAboutActivity extends BaseFragment {

    public static final String CHANNEL_USERNAME = "vinogram_android";
    public static final String TRANSLATIONS_URL = "https://translations.telegram.org";
    public static final String SOURCE_URL = "https://github.com/fumaz0/vinogram-android";

    private static final int VIEW_TYPE_HERO = 0;
    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_ROW = 2;
    private static final int VIEW_TYPE_INFO = 3;

    private static final int ITEM_HERO = 0;
    private static final int ITEM_CATEGORIES_HEADER = 1;
    private static final int ITEM_MAIN = 2;
    private static final int ITEM_APPEARANCE = 3;
    private static final int ITEM_OTHER = 4;
    private static final int ITEM_LINKS_HEADER = 5;
    private static final int ITEM_CHANNEL = 6;
    private static final int ITEM_TRANSLATION = 7;
    private static final int ITEM_SOURCE = 8;
    private static final int ITEM_FOOTER = 9;
    private static final int ITEM_COUNT = 10;

    private RecyclerListView listView;

    public static String getAppVersion() {
        try {
            final PackageInfo info = ApplicationLoader.applicationContext
                    .getPackageManager()
                    .getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
            return info.versionName == null ? "" : info.versionName;
        } catch (Exception e) {
            return "";
        }
    }

    public static CharSequence getVersionText() {
        return LocaleController.formatString(R.string.VinogramAboutVersion, getAppVersion());
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString(R.string.VinogramAbout));
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

        final FrameLayout contentView = new FrameLayout(context);
        contentView.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));

        listView = new RecyclerListView(context);
        listView.setSections(true);
        actionBar.setAdaptiveBackground(listView);
        listView.setLayoutManager(new LinearLayoutManager(context));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(new ListAdapter(context));
        listView.setOnItemClickListener((view, position, x, y) -> onRowClick(position));
        contentView.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        fragmentView = contentView;
        return fragmentView;
    }

    private void openUrl(String url) {
        if (getParentActivity() == null || TextUtils.isEmpty(url)) {
            return;
        }
        Browser.openUrl(getParentActivity(), url);
    }

    private void onRowClick(int position) {
        if (position == ITEM_MAIN) {
            finishFragment();
        } else if (position == ITEM_APPEARANCE) {
            presentFragment(new ThemeActivity(ThemeActivity.THEME_TYPE_BASIC));
        } else if (position == ITEM_OTHER) {
            openUrl(LocaleController.getString(R.string.TelegramFaqUrl));
        } else if (position == ITEM_CHANNEL) {
            MessagesController.getInstance(currentAccount).openByUserName(CHANNEL_USERNAME, this, 1);
        } else if (position == ITEM_TRANSLATION) {
            openUrl(TRANSLATIONS_URL);
        } else if (position == ITEM_SOURCE) {
            openUrl(SOURCE_URL);
        }
    }

    private class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final Context context;

        private ListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getItemCount() {
            return ITEM_COUNT;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == ITEM_HERO) {
                return VIEW_TYPE_HERO;
            } else if (position == ITEM_CATEGORIES_HEADER || position == ITEM_LINKS_HEADER) {
                return VIEW_TYPE_HEADER;
            } else if (position == ITEM_FOOTER) {
                return VIEW_TYPE_INFO;
            }
            return VIEW_TYPE_ROW;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final View view;
            if (viewType == VIEW_TYPE_HERO) {
                view = new HeroView(context);
            } else if (viewType == VIEW_TYPE_HEADER) {
                view = new HeaderCell(context);
            } else if (viewType == VIEW_TYPE_ROW) {
                view = new RowCell(context);
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
            final int viewType = holder.getItemViewType();
            if (viewType == VIEW_TYPE_HEADER) {
                ((HeaderCell) holder.itemView).setText(LocaleController.getString(
                        position == ITEM_CATEGORIES_HEADER ? R.string.VinogramAboutCategories : R.string.VinogramAboutLinks));
            } else if (viewType == VIEW_TYPE_ROW) {
                final RowCell cell = (RowCell) holder.itemView;
                if (position == ITEM_MAIN) {
                    cell.set(R.drawable.msg_settings, getString(R.string.VinogramAboutMain), null, true);
                } else if (position == ITEM_APPEARANCE) {
                    cell.set(R.drawable.msg_theme, getString(R.string.VinogramAboutAppearance), null, true);
                } else if (position == ITEM_OTHER) {
                    cell.set(R.drawable.msg_info, getString(R.string.VinogramAboutOther), null, false);
                } else if (position == ITEM_CHANNEL) {
                    cell.set(R.drawable.msg_channel, getString(R.string.VinogramAboutChannel), getString(R.string.VinogramAboutChannelValue), true);
                } else if (position == ITEM_TRANSLATION) {
                    cell.set(R.drawable.msg_translate, getString(R.string.VinogramAboutTranslation), "translations.telegram.org", true);
                } else if (position == ITEM_SOURCE) {
                    cell.set(R.drawable.msg_link2, getString(R.string.VinogramAboutSourceCode), "github.com/fumaz0", false);
                }
            } else if (viewType == VIEW_TYPE_INFO) {
                final TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                cell.setText(getString(R.string.VinogramAboutFooter));
                cell.setBackground(null);
            }
        }
    }

    private class HeroView extends LinearLayout {

        private HeroView(Context context) {
            super(context);
            setOrientation(VERTICAL);
            setGravity(Gravity.CENTER_HORIZONTAL);
            setPadding(0, dp(28), 0, dp(24));

            final ImageView logoView = new ImageView(context);
            logoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            logoView.setImageResource(R.drawable.vinogram_logo);
            logoView.setContentDescription(LocaleController.getString(R.string.AppName));
            addView(logoView, LayoutHelper.createLinear(96, 96, Gravity.CENTER_HORIZONTAL));

            final TextView nameView = new TextView(context);
            nameView.setText(LocaleController.getString(R.string.AppName));
            nameView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
            nameView.setTypeface(AndroidUtilities.bold());
            nameView.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
            nameView.setGravity(Gravity.CENTER);
            nameView.setSingleLine(true);
            addView(nameView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 12, 0, 0));

            final TextView versionView = new TextView(context);
            versionView.setText(getVersionText());
            versionView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            versionView.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteGrayText));
            versionView.setGravity(Gravity.CENTER);
            versionView.setSingleLine(true);
            addView(versionView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 4, 0, 0));

            final TextView descriptionView = new TextView(context);
            descriptionView.setText(getString(R.string.VinogramAboutDescription));
            descriptionView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            descriptionView.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteGrayText));
            descriptionView.setGravity(Gravity.CENTER);
            descriptionView.setLineSpacing(dp(3), 1f);
            addView(descriptionView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 32, 12, 32, 0));
        }
    }

    private class RowCell extends FrameLayout {

        private final ImageView iconView;
        private final TextView textView;
        private final TextView valueView;
        private boolean needDivider;

        private RowCell(Context context) {
            super(context);

            iconView = new ImageView(context);
            iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            iconView.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.MULTIPLY));
            addView(iconView, LayoutHelper.createFrame(24, 24,
                    (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL,
                    LocaleController.isRTL ? 0 : 21, 0, LocaleController.isRTL ? 21 : 0, 0));

            textView = new TextView(context);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            textView.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
            textView.setSingleLine(true);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
            addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT,
                    (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP,
                    LocaleController.isRTL ? 0 : 59, 0, LocaleController.isRTL ? 59 : 0, 0));

            valueView = new TextView(context);
            valueView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            valueView.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteValueText));
            valueView.setSingleLine(true);
            valueView.setEllipsize(TextUtils.TruncateAt.END);
            valueView.setGravity((LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL);
            valueView.setVisibility(GONE);
            addView(valueView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT,
                    (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP,
                    21, 0, 21, 0));
        }

        private void set(int icon, CharSequence text, CharSequence value, boolean divider) {
            iconView.setImageResource(icon);
            textView.setText(text);
            valueView.setText(value);
            valueView.setVisibility(TextUtils.isEmpty(value) ? GONE : VISIBLE);
            needDivider = divider;
            setWillNotDraw(!divider);
            requestLayout();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            final int width = MeasureSpec.getSize(widthMeasureSpec);
            final int height = dp(50);
            setMeasuredDimension(width, height);

            iconView.measure(
                    MeasureSpec.makeMeasureSpec(dp(24), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(dp(24), MeasureSpec.EXACTLY));

            int valueWidth = 0;
            if (valueView.getVisibility() == VISIBLE) {
                valueView.measure(
                        MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST),
                        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
                valueWidth = valueView.getMeasuredWidth() + dp(12);
            }
            final int textWidth = Math.max(0, width - dp(59) - dp(21) - valueWidth);
            textView.measure(
                    MeasureSpec.makeMeasureSpec(textWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (needDivider) {
                final float y = getMeasuredHeight() - 1;
                if (LocaleController.isRTL) {
                    canvas.drawLine(0, y, getMeasuredWidth() - dp(59), y, Theme.dividerPaint);
                } else {
                    canvas.drawLine(dp(59), y, getMeasuredWidth(), y, Theme.dividerPaint);
                }
            }
        }
    }
}