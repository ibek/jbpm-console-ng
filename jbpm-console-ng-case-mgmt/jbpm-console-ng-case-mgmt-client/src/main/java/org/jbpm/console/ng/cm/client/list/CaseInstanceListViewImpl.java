/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.console.ng.cm.client.list;

import java.util.LinkedList;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.NoSelectionModel;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jbpm.console.ng.cm.model.CaseInstanceSummary;
import org.jbpm.console.ng.gc.client.experimental.grid.base.ExtendedPagedTable;
import org.jbpm.console.ng.gc.client.list.base.AbstractListView;
import org.jbpm.console.ng.gc.client.util.ButtonActionCell;
import org.jbpm.console.ng.gc.client.util.DateUtils;
import org.uberfire.ext.services.shared.preferences.GridGlobalPreferences;
import org.uberfire.ext.widgets.table.client.ColumnMeta;

import static java.util.Arrays.*;
import static org.jbpm.console.ng.cm.client.resources.i18n.Constants.*;

@Dependent
public class CaseInstanceListViewImpl extends AbstractListView<CaseInstanceSummary, CaseInstanceListPresenter>
        implements CaseInstanceListPresenter.CaseInstanceListView {

    public static final String COL_ID_CASE_ID = "caseId";
    public static final String COL_ID_DESCRIPTION = "description";
    public static final String COL_ID_OWNER = "owner";
    public static final String COL_ID_STARTED_AT = "startedAt";
    public static final String COL_ID_STATUS = "status";
    public static final String COL_ID_ACTIONS = "Actions";
    public static final String CASE_INSTANCE_LIST_GRID = "CaseInstanceListGrid";

    @Inject
    private TranslationService translationService;

    @Override
    public void init(final CaseInstanceListPresenter presenter) {
        final List<String> bannedColumns = asList(COL_ID_CASE_ID, COL_ID_ACTIONS);
        final List<String> initColumns = asList(COL_ID_CASE_ID, COL_ID_DESCRIPTION, COL_ID_STATUS, COL_ID_OWNER, COL_ID_STARTED_AT, COL_ID_ACTIONS);
        super.init(presenter, new GridGlobalPreferences(CASE_INSTANCE_LIST_GRID, initColumns, bannedColumns));

        selectionModel = new NoSelectionModel<>();
        selectionModel.addSelectionChangeHandler(e -> {
            if (selectedRow == -1) {
                selectedRow = listGrid.getKeyboardSelectedRow();
                listGrid.setRowStyles(selectedStyles);
                listGrid.redraw();
            } else if (listGrid.getKeyboardSelectedRow() != selectedRow) {
                listGrid.setRowStyles(selectedStyles);
                selectedRow = listGrid.getKeyboardSelectedRow();
                listGrid.redraw();
            }

            presenter.selectCaseInstance(selectionModel.getLastSelectedObject());
        });

        noActionColumnManager = DefaultSelectionEventManager
                .createCustomManager(new DefaultSelectionEventManager.EventTranslator<CaseInstanceSummary>() {

                    @Override
                    public boolean clearCurrentSelection(CellPreviewEvent<CaseInstanceSummary> event) {
                        return false;
                    }

                    @Override
                    public DefaultSelectionEventManager.SelectAction translateSelectionEvent(CellPreviewEvent<CaseInstanceSummary> event) {
                        NativeEvent nativeEvent = event.getNativeEvent();
                        if (BrowserEvents.CLICK.equals(nativeEvent.getType()) &&
                                // Ignore if the event didn't occur in the correct column.
                                listGrid.getColumnIndex(actionsColumn) == event.getColumn()) {
                            return DefaultSelectionEventManager.SelectAction.IGNORE;
                        }
                        return DefaultSelectionEventManager.SelectAction.DEFAULT;
                    }
                });
        listGrid.setSelectionModel(selectionModel, noActionColumnManager);
        listGrid.setEmptyTableCaption(translationService.format(NO_CASES_FOUND));
        listGrid.setRowStyles(selectedStyles);

        listGrid.getElement().getStyle().setPaddingRight(20, Style.Unit.PX);
        listGrid.getElement().getStyle().setPaddingLeft(20, Style.Unit.PX);
    }

    @Override
    public void initColumns(final ExtendedPagedTable<CaseInstanceSummary> table) {
        initCellPreview();
        final Column idColumn = initIdColumn();
        final Column descriptionColumn = initDescriptionColumn();
        final Column statusColumn = initStatusColumn();
        final Column ownerColumn = initOwnerColumn();
        final Column startedAtColumn = initStartedAtColumn();
        actionsColumn = initActionsColumn();

        table.addColumns(asList(
                new ColumnMeta<>(idColumn, translationService.format(ID)),
                new ColumnMeta<>(descriptionColumn, translationService.format(DESCRIPTION)),
                new ColumnMeta<>(statusColumn, translationService.format(STATUS)),
                new ColumnMeta<>(ownerColumn, translationService.format(OWNER)),
                new ColumnMeta<>(startedAtColumn, translationService.format(STARTED_AT)),
                new ColumnMeta<>(actionsColumn, translationService.format(ACTIONS))
        ));
    }

    private void initCellPreview() {
        listGrid.addCellPreviewHandler(event -> {
            if (BrowserEvents.MOUSEOVER.equalsIgnoreCase(event.getNativeEvent().getType())) {
                onMouseOverGrid(event);
            }
        });
    }

    private void onMouseOverGrid(final CellPreviewEvent<CaseInstanceSummary> event) {
        final CaseInstanceSummary caseInstance = event.getValue();
        if (caseInstance.getDescription() != null) {
            listGrid.setTooltip(listGrid.getKeyboardSelectedRow(), event.getColumn(), caseInstance.getDescription());
        }
    }

    private Column initIdColumn() {
        final Column<CaseInstanceSummary, String> caseIdColumn = new Column<CaseInstanceSummary, String>(new TextCell()) {
            @Override
            public String getValue(final CaseInstanceSummary cis) {
                return cis.getCaseId();
            }
        };
        caseIdColumn.setSortable(true);
        caseIdColumn.setDataStoreName(COL_ID_CASE_ID);
        return caseIdColumn;
    }

    private Column initDescriptionColumn() {
        final Column<CaseInstanceSummary, String> descriptionColumn = new Column<CaseInstanceSummary, String>(new TextCell()) {
            @Override
            public String getValue(final CaseInstanceSummary cis) {
                return cis.getDescription();
            }
        };
        descriptionColumn.setSortable(true);
        descriptionColumn.setDataStoreName(COL_ID_DESCRIPTION);
        return descriptionColumn;
    }

    private Column initOwnerColumn() {
        final Column<CaseInstanceSummary, String> ownerColumn = new Column<CaseInstanceSummary, String>(new TextCell()) {
            @Override
            public String getValue(final CaseInstanceSummary cis) {
                return cis.getOwner();
            }
        };
        ownerColumn.setSortable(true);
        ownerColumn.setDataStoreName(COL_ID_OWNER);
        return ownerColumn;
    }

    private Column initStartedAtColumn() {
        final Column<CaseInstanceSummary, String> startedAtColumn = new Column<CaseInstanceSummary, String>(new TextCell()) {
            @Override
            public String getValue(final CaseInstanceSummary cis) {
                return DateUtils.getDateTimeStr(cis.getStartedAt());
            }
        };
        startedAtColumn.setSortable(true);
        startedAtColumn.setDataStoreName(COL_ID_STARTED_AT);
        return startedAtColumn;
    }

    private Column initStatusColumn() {
        final Column<CaseInstanceSummary, String> statusColumn = new Column<CaseInstanceSummary, String>(new TextCell()) {
            @Override
            public String getValue(final CaseInstanceSummary cis) {
                return translationService.format(cis.getStatusString());
            }
        };
        statusColumn.setSortable(true);
        statusColumn.setDataStoreName(COL_ID_STATUS);
        return statusColumn;
    }

    private Column initActionsColumn() {
        final List<HasCell<CaseInstanceSummary, ?>> cells = new LinkedList<>();

        cells.add(new CancelActionHasCell(translationService.format(COMPLETE), (CaseInstanceSummary caseInstanceSummary) ->
                presenter.cancelCaseInstance(caseInstanceSummary)
        ));

        cells.add(new DestroyActionHasCell(translationService.format(CLOSE), (CaseInstanceSummary caseInstanceSummary) ->
                presenter.destroyCaseInstance(caseInstanceSummary)
        ));

        final CompositeCell<CaseInstanceSummary> cell = new CompositeCell<>(cells);
        final Column<CaseInstanceSummary, CaseInstanceSummary> actionsColumn = new Column<CaseInstanceSummary, CaseInstanceSummary>(cell) {
            @Override
            public CaseInstanceSummary getValue(final CaseInstanceSummary caseInstanceSummary) {
                return caseInstanceSummary;
            }
        };

        actionsColumn.setDataStoreName(COL_ID_ACTIONS);
        return actionsColumn;
    }

    protected class CancelActionHasCell extends ButtonActionCell<CaseInstanceSummary> {

        public CancelActionHasCell(final String text, final ActionCell.Delegate<CaseInstanceSummary> delegate) {
            super(text, delegate);
        }

        @Override
        public void render(final Cell.Context context, final CaseInstanceSummary caseInstanceSummary, final SafeHtmlBuilder sb) {
            if (caseInstanceSummary.isActive()) {
                super.render(context, caseInstanceSummary, sb);
            }
        }
    }

    protected class DestroyActionHasCell extends ButtonActionCell<CaseInstanceSummary> {

        public DestroyActionHasCell(final String text, final ActionCell.Delegate<CaseInstanceSummary> delegate) {
            super(text, delegate);
        }

        @Override
        public void render(final Cell.Context context, final CaseInstanceSummary caseInstanceSummary, final SafeHtmlBuilder sb) {
            if (caseInstanceSummary.isActive()) {
                super.render(context, caseInstanceSummary, sb);
            }
        }
    }

}