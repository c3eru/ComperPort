//
// Decompiled by Jadx - 2220ms
//
package com.android.systemui.controlcenter.phone;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.preference.SettingsHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.controlcenter.phone.-$;
import com.android.systemui.controlcenter.phone.ControlPanelWindowManager;
import com.android.systemui.controlcenter.qs.tileview.CCQSTileView;
import com.android.systemui.controlcenter.utils.FolmeAnimState;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.external.CustomTile;
import com.miui.systemui.util.CommonUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class QSControlCenterTileLayout extends ViewGroup implements QSPanel.QSTileLayout, QSHost.Callback, ControlPanelWindowManager.OnExpandChangeListener, FolmeAnimState {
    private int mBaseLineIdx;
    private int mCellHeight;
    private int mCellWidth;
    private int mColumnMarginTop;
    private int mColumns;
    private Context mContext;
    private int mExpandHeightThres = 1;
    private boolean mExpanded;
    private boolean mExpanding;
    private final H mHandler = new H(this, (QSControlCenterTileLayout$1) null);
    private QSTileHost mHost;
    private int mLastCellPaddingBottom;
    private float mLastHeight = -1.0f;
    private boolean mListening;
    private int mMaxHeight;
    private int mMinCellHeight;
    private int mMinHeight;
    private int mMinShowRows;
    private float mNewHeight = -1.0f;
    private float mOffset;
    private int mOrientation;
    private ControlPanelController mPanelController;
    private float mPanelLandWidth;
    private float mPanelPaddingHorizontal;
    private ControlCenterPanelView mPanelView;
    protected final ArrayList<QSPanel.TileRecord> mRecords = new ArrayList<>();
    private int mRowMarginStart;
    private int mShowLines;

    public QSControlCenterTileLayout(Context context) {
        super(context, (AttributeSet) null);
    }

    public QSControlCenterTileLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
        this.mPanelController = (ControlPanelController) Dependency.get(ControlPanelController.class);
        updateResources();
    }

    static /* synthetic */ int access$100(QSControlCenterTileLayout qSControlCenterTileLayout) {
        return qSControlCenterTileLayout.mBaseLineIdx;
    }

    static /* synthetic */ H access$200(QSControlCenterTileLayout qSControlCenterTileLayout) {
        return qSControlCenterTileLayout.mHandler;
    }

    private void endExpanding() {
        this.mExpanding = false;
    }

    private static int exactly(int i) {
        return View.MeasureSpec.makeMeasureSpec(i, 0x40000000);
    }

    private int getColumn() {
        if (getResources().getConfiguration().orientation == 2) {
            return 4;
        }
        return SettingsHelper.getIntofSettings("qc_controlcenter_column", 4);
    }

    private int getColumnStart(int i) {
        return i * (this.mCellWidth + this.mRowMarginStart);
    }

    private int getRowBottom(QSPanel.TileRecord tileRecord, int i, int i2) {
        QSTileView qSTileView = tileRecord.tileView;
        if (i >= this.mMinShowRows) {
            return i2 + this.mCellHeight;
        }
        return i2 + (this.mNewHeight == ((float) this.mMinHeight) ? this.mMinCellHeight : this.mCellHeight);
    }

    private int getRowTop(QSPanel.TileRecord tileRecord, int i) {
        if (i == 0) {
            return 0;
        }
        QSTileView qSTileView = this.mRecords.get(this.mColumns * (i - 1)).tileView;
        if (i >= this.mMinShowRows || i == 0) {
            return qSTileView.getBottom() + this.mColumnMarginTop;
        }
        if (this.mExpanding) {
            float pow = (float) (1.0d - Math.pow((double) (1.0f - (this.mOffset / ((float) this.mExpandHeightThres))), 3.0d));
            int i2 = this.mMinCellHeight;
            return (int) ((((float) (this.mColumnMarginTop + i2)) + (((float) (this.mCellHeight - i2)) * pow)) * ((float) i));
        }
        tileRecord.tileView.getTop();
        return ((this.mExpanded ? this.mCellHeight : this.mMinCellHeight) + this.mColumnMarginTop) * i;
    }

    private /* synthetic */ void lambda$performAttachedToWindow$0() {
        setTiles(this.mHost.getTiles());
    }

    private /* synthetic */ void lambda$setTiles$1() {
        setExpanded(this.mExpanded);
    }

    private void updateLayout() {
        Log.d("QSControlCenterTileLayout", "updateWidth orientation=" + this.mOrientation);
        this.mColumnMarginTop = this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_center_tile_margin_top);
        if (this.mOrientation == 2) {
            float f = this.mPanelLandWidth;
            int i = this.mCellWidth;
            int i2 = this.mColumns;
            this.mRowMarginStart = ((int) (f - ((float) (i * i2)))) / (i2 - 1);
            requestLayout();
            return;
        }
        Point screenSize = CommonUtil.getScreenSize(this.mContext);
        if (screenSize != null) {
            float f2 = ((float) screenSize.x) - (this.mPanelPaddingHorizontal * 2.0f);
            int i3 = this.mCellWidth;
            int i4 = this.mColumns;
            this.mRowMarginStart = ((int) (f2 - ((float) (i3 * i4)))) / (i4 - 1);
            requestLayout();
        }
    }

    private void updateViewsLine() {
        Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            QSPanel.TileRecord next = it.next();
            next.tileView.setTag(R.id.tag_tile_layout, Integer.valueOf(this.mRecords.indexOf(next) / this.mColumns));
        }
        int ceil = (int) Math.ceil((double) (((float) this.mRecords.size()) / ((float) this.mColumns)));
        int i = this.mMinCellHeight;
        int i2 = this.mMinShowRows;
        int i3 = this.mColumnMarginTop;
        int i4 = (i * i2) + ((i2 - 1) * i3);
        int i5 = this.mLastCellPaddingBottom;
        this.mMinHeight = i4 + i5;
        this.mMaxHeight = (this.mCellHeight * ceil) + ((ceil - 1) * i3) + i5;
    }

    protected QSPanel.TileRecord addTile(QSTile qSTile, boolean z) {
        QSPanel.TileRecord tileRecord = new QSPanel.TileRecord();
        tileRecord.tile = qSTile;
        tileRecord.tileView = createTileView(qSTile, !this.mExpanded && this.mRecords.size() <= this.mMinShowRows * this.mColumns);
        QSControlCenterTileLayout$3 r4 = new QSControlCenterTileLayout$3(this, tileRecord);
        tileRecord.tile.addCallback(r4);
        tileRecord.callback = r4;
        tileRecord.tileView.init(tileRecord.tile);
        tileRecord.tile.refreshState();
        addTile(tileRecord);
        return tileRecord;
    }

    public void addTile(QSPanel.TileRecord tileRecord) {
        this.mRecords.add(tileRecord);
        tileRecord.tile.setListening(this, this.mListening);
        addView(tileRecord.tileView, new ViewGroup.LayoutParams(-2, -2));
        updateViewsLine();
    }

    public int calculateExpandHeight() {
        return getMaxHeight() - getMinHeight();
    }

    public void clickTile(ComponentName componentName) {
        String spec = CustomTile.toSpec(componentName);
        int size = this.mRecords.size();
        for (int i = 0; i < size; i++) {
            if (this.mRecords.get(i).tile.getTileSpec().equals(spec)) {
                this.mRecords.get(i).tile.click();
                return;
            }
        }
    }

    protected QSTileView createTileView(QSTile qSTile, boolean z) {
        return this.mHost.getHostInjector().createControlCenterTileView(qSTile, z);
    }

    protected void drawTile(QSPanel.TileRecord tileRecord, QSTile.State state) {
        tileRecord.tileView.onStateChanged(state);
    }

    public int getMaxHeight() {
        return this.mMaxHeight;
    }

    public int getMinHeight() {
        return this.mMinHeight;
    }

    public int getNumVisibleTiles() {
        return 0;
    }

    public int getOffsetTop(QSPanel.TileRecord tileRecord) {
        return 0;
    }

    public int getShowLines() {
        return this.mShowLines;
    }

    public View[] getVisAnimViews() {
        View[] viewArr = new View[this.mRecords.size()];
        for (int i = 0; i < this.mRecords.size(); i++) {
            viewArr[i] = this.mRecords.get(i).tileView;
        }
        return viewArr;
    }

    public boolean isExpanded() {
        return this.mExpanded;
    }

    public /* synthetic */ void lambda$performAttachedToWindow$0$QSControlCenterTileLayout() {
        lambda$performAttachedToWindow$0();
    }

    public /* synthetic */ void lambda$setTiles$1$QSControlCenterTileLayout() {
        lambda$setTiles$1();
    }

    protected void onConfigurationChanged(Configuration configuration) {
        Log.d("QSControlCenterTileLayout", "onConfigurationChanged orientation=" + this.mOrientation + "  newConfig.orientation=" + configuration.orientation);
        super.onConfigurationChanged(configuration);
        int i = this.mOrientation;
        int i2 = configuration.orientation;
        if (i != i2) {
            this.mOrientation = i2;
            updateResources();
            updateLayout();
        }
    }

    public void onExpandChange(boolean z) {
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mOrientation = this.mContext.getResources().getConfiguration().orientation;
        updateLayout();
    }

    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        int i7 = 0;
        boolean z2 = true;
        if (getLayoutDirection() != 1) {
            z2 = false;
        }
        int i8 = 0;
        int i9 = 0;
        while (i7 < this.mRecords.size()) {
            int i10 = this.mColumns;
            if (i8 == i10) {
                i9++;
                i8 -= i10;
            }
            QSPanel.TileRecord tileRecord = this.mRecords.get(i7);
            int rowTop = getRowTop(tileRecord, i9);
            int rowBottom = getRowBottom(tileRecord, i9, rowTop);
            if (z2) {
                i6 = getColumnStart(this.mColumns - i8) - this.mRowMarginStart;
                i5 = i6 - this.mCellWidth;
            } else {
                i5 = getColumnStart(i8);
                i6 = this.mCellWidth + i5;
            }
            tileRecord.tileView.layout(i5, rowTop, i6, rowBottom);
            i7++;
            i8++;
        }
    }

    protected void onMeasure(int i, int i2) {
        int size = View.MeasureSpec.getSize(i);
        View.MeasureSpec.getSize(i2);
        Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
        View view = this;
        while (it.hasNext()) {
            QSPanel.TileRecord next = it.next();
            if (next.tileView.getVisibility() != 8) {
                next.tileView.measure(exactly(this.mCellWidth), exactly(this.mCellHeight));
                view = next.tileView.updateAccessibilityOrder(view);
            }
        }
        this.mShowLines = this.mNewHeight > ((float) this.mMinHeight) ? (int) Math.ceil((double) (((float) this.mRecords.size()) / ((float) this.mColumns))) : this.mMinShowRows;
        float f = this.mNewHeight;
        int i3 = this.mMinHeight;
        if (f > ((float) i3)) {
            i3 = this.mMaxHeight;
        }
        setMeasuredDimension(size, i3);
        StringBuilder sb = new StringBuilder();
        sb.append("onMeasure height:");
        float f2 = this.mNewHeight;
        int i4 = this.mMinHeight;
        if (f2 > ((float) i4)) {
            i4 = this.mMaxHeight;
        }
        sb.append(i4);
        sb.append("  showLines:");
        sb.append(this.mShowLines);
        Log.d("QSControlCenterTileLayout", sb.toString());
    }

    public void onTilesChanged() {
        setTiles(this.mHost.getTiles());
    }

    public void performAttachedToWindow() {
        ((ControlPanelWindowManager) Dependency.get(ControlPanelWindowManager.class)).addExpandChangeListener(this);
        if (this.mHost != null) {
            this.mHandler.post(new -$.Lambda.QSControlCenterTileLayout.CxAivZjJmzWXIhy6v5-hEnbQh9Y(this));
        }
    }

    public void performDetachedFromWindow() {
        QSTileHost qSTileHost = this.mHost;
        if (qSTileHost != null) {
            qSTileHost.removeCallback(this);
        }
        ((ControlPanelWindowManager) Dependency.get(ControlPanelWindowManager.class)).removeExpandChangeListener(this);
        Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            it.next().tile.removeCallbacksByType(3);
        }
    }

    public void refreshAllTiles() {
        Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            QSPanel.TileRecord next = it.next();
            next.tile.refreshState();
            next.tileView.updateResources();
        }
    }

    public void removeTile(QSPanel.TileRecord tileRecord) {
        this.mRecords.remove(tileRecord);
        tileRecord.tile.setListening(this, false);
        removeView(tileRecord.tileView);
        updateViewsLine();
    }

    public void requestLayout() {
        if (!isInLayout() && getParent() != null && getParent().isLayoutRequested()) {
            for (ViewParent parent = getParent(); parent != null; parent = parent.getParent()) {
                if (parent instanceof View) {
                    ((View) parent).mPrivateFlags &= -4097;
                }
            }
        }
        super.requestLayout();
    }

    public void setBaseLineIdx(int i) {
        this.mBaseLineIdx = i;
    }

    public void setExpandRatio(float f) {
        this.mExpanded = false;
        this.mExpanding = true;
        this.mOffset = ((float) this.mExpandHeightThres) * f;
        this.mNewHeight = Math.max(Math.min(this.mLastHeight + ((float) ((int) (((float) calculateExpandHeight()) * f))), (float) this.mMaxHeight), (float) this.mMinHeight);
        Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            QSPanel.TileRecord next = it.next();
            int intValue = ((Integer) next.tileView.getTag(R.id.tag_tile_layout)).intValue();
            CCQSTileView cCQSTileView = next.tileView;
            QSIconView icon = cCQSTileView.getIcon();
            cCQSTileView.getLabel();
            if (intValue < this.mMinShowRows) {
                cCQSTileView.setLabelAlpha(Math.min(1.0f, f));
            } else {
                cCQSTileView.setVisibility(0);
                double d = (double) f;
                icon.setAlpha(Math.min(1.0f, (float) Math.pow(d, (double) ((((intValue - this.mMinShowRows) * 2) + 1) * 2))));
                cCQSTileView.setLabelAlpha(Math.min(1.0f, (float) Math.pow(d, (double) ((((intValue - this.mMinShowRows) * 2) + 2) * 2))));
            }
        }
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = (int) this.mNewHeight;
        setLayoutParams(layoutParams);
    }

    public void setExpanded(boolean z) {
        if (!this.mPanelController.isSuperPowerMode()) {
            this.mExpanded = z;
            this.mExpanding = false;
            this.mOffset = 0.0f;
            float f = (float) (z ? this.mMaxHeight : this.mMinHeight);
            this.mLastHeight = f;
            this.mNewHeight = f;
            visStartInit();
            Log.d("QSControlCenterTileLayout", "setExpanded:" + z);
            requestLayout();
            ControlCenterPanelView controlCenterPanelView = this.mPanelView;
            if (controlCenterPanelView != null) {
                controlCenterPanelView.notifyTileChanged();
            }
        }
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
        qSTileHost.addCallback(this);
        setTiles(this.mHost.getTiles());
    }

    public void setListening(boolean z) {
        if (this.mListening != z) {
            this.mListening = z;
            Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
            while (it.hasNext()) {
                it.next().tile.setListening(this, z);
            }
        }
    }

    public void setPanelView(ControlCenterPanelView controlCenterPanelView) {
        this.mPanelView = controlCenterPanelView;
    }

    public void setTiles(Collection<QSTile> collection) {
        setTiles(collection, this.mExpanded);
        post(new -$.Lambda.QSControlCenterTileLayout.alKZfw2hiqUjeWLs5O4DK218ufc(this));
    }

    public void setTiles(Collection<QSTile> collection, boolean z) {
        ArrayList arrayList = new ArrayList(collection);
        Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            QSPanel.TileRecord next = it.next();
            next.tile.removeCallback(next.callback);
            next.tile.setListening(this, false);
            removeView(next.tileView);
        }
        this.mRecords.clear();
        Iterator it2 = arrayList.iterator();
        while (it2.hasNext()) {
            QSTile qSTile = (QSTile) it2.next();
            if (!qSTile.getTileSpec().equals("edit")) {
                addTile(qSTile, z);
            }
        }
        updateViewsLine();
    }

    public boolean updateResources() {
        this.mPanelPaddingHorizontal = (float) this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_panel_margin_horizontal);
        this.mPanelLandWidth = (float) this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_width_land);
        this.mLastCellPaddingBottom = this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_tile_label_padding_top);
        Resources resources = this.mContext.getResources();
        int column = getColumn();
        if (this.mColumns != column) {
            this.mColumns = column;
            Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
            while (it.hasNext()) {
                QSPanel.TileRecord next = it.next();
                next.tileView.setTag(R.id.tag_tile_layout, Integer.valueOf(this.mRecords.indexOf(next) / this.mColumns));
            }
            int ceil = (int) Math.ceil((double) (((float) this.mRecords.size()) / ((float) this.mColumns)));
            int i = this.mMinCellHeight;
            int i2 = this.mMinShowRows;
            int i3 = this.mColumnMarginTop;
            int i4 = (i * i2) + ((i2 - 1) * i3);
            int i5 = this.mLastCellPaddingBottom;
            this.mMinHeight = i4 + i5;
            this.mMaxHeight = (this.mCellHeight * ceil) + ((ceil - 1) * i3) + i5;
        }
        this.mMinShowRows = this.mPanelController.isSuperPowerMode() ? 1 : this.mContext.getResources().getInteger(R.dimen.qs_control_tiles_min_rows);
        this.mColumnMarginTop = this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_center_tile_margin_top);
        this.mCellWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_center_tile_width);
        this.mCellHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_center_tile_height) + CCQSTileView.getTextHeight(getContext());
        int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_tile_icon_bg_size);
        this.mMinCellHeight = dimensionPixelSize;
        int i6 = this.mMinShowRows;
        int i7 = (dimensionPixelSize * i6) + ((i6 - 1) * this.mColumnMarginTop) + this.mLastCellPaddingBottom;
        this.mMinHeight = i7;
        if (i7 < 0) {
            float f = (float) i7;
            this.mLastHeight = f;
            this.mNewHeight = f;
        }
        if (this.mListening) {
            refreshAllTiles();
        }
        return true;
    }

    public void updateTransHeight(List<View> list, float f, int i, int i2) {
        if (f == 0.0f) {
            int size = this.mRecords.size() + (list == null ? 0 : list.size());
            View[] viewArr = new View[size];
            for (int i3 = 0; i3 < this.mRecords.size(); i3++) {
                viewArr[i3] = this.mRecords.get(i3).tileView;
            }
            for (int size2 = this.mRecords.size(); size2 < size; size2++) {
                viewArr[size2] = list.get(size2 - this.mRecords.size());
            }
            post(new QSControlCenterTileLayout$1(this, viewArr));
            endExpanding();
            return;
        }
        post(new QSControlCenterTileLayout$2(this, i2, Math.max(0.0f, Math.min(f, (float) i)), i));
    }

    public void visStartInit() {
        Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            QSPanel.TileRecord next = it.next();
            float f = 0.0f;
            if (((Integer) next.tileView.getTag(R.id.tag_tile_layout)).intValue() < this.mMinShowRows) {
                next.tileView.setAlpha(1.0f);
                next.tileView.getIcon().setAlpha(1.0f);
                CCQSTileView cCQSTileView = next.tileView;
                if (this.mExpanded) {
                    f = 1.0f;
                }
                cCQSTileView.setLabelAlpha(f);
            } else {
                next.tileView.setAlpha(1.0f);
                CCQSTileView cCQSTileView2 = next.tileView;
                if (this.mExpanded) {
                    f = 1.0f;
                }
                cCQSTileView2.setChildsAlpha(f);
            }
        }
    }
}
