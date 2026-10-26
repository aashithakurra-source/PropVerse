package com.propverse.ui;

import com.propverse.dao.RecordDAO;
import com.propverse.util.InputValidator;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/** Shared Swing CRUD form for the four database-backed modules. */
public class ModuleFrame extends JPanel {
    private final RecordDAO dao = new RecordDAO();
    private final RecordDAO.Spec spec;
    private final JComponent[] editors;
    private final JComboBox<RecordDAO.Choice>[] filters;
    private final JTable table = new JTable();
    private final JTextField search = new JTextField(14);
    private Integer selected;
    private boolean loadingLookups;

    @SuppressWarnings("unchecked")
    public ModuleFrame(RecordDAO.Spec spec) {
        this.spec = spec;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 5));
        editors = new JComponent[spec.columns().length];
        for (int i = 0; i < editors.length; i++) {
            form.add(new JLabel(spec.labels()[i]));
            editors[i] = editor(spec.columns()[i]);
            if ("progress_percentage".equals(spec.columns()[i]) && editors[i] instanceof JTextField progress) progress.setText("0");
            form.add(editors[i]);
        }
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = new JButton("Add"), update = new JButton("Update"), delete = new JButton("Delete"), clear = new JButton("Clear"), refresh = new JButton("Refresh"), find = new JButton("Search");
        controls.add(add); controls.add(update); controls.add(delete); controls.add(clear); controls.add(refresh); controls.add(search); controls.add(find);
        filters = new JComboBox[spec.filterColumns().length];
        for (int i = 0; i < filters.length; i++) {
            controls.add(new JLabel(spec.filterLabels()[i]));
            filters[i] = new JComboBox<>(); filters[i].addItem(new RecordDAO.Choice("", "All")); filters[i].addActionListener(e -> { if(!loadingLookups) load(); });
            controls.add(filters[i]);
        }
        JPanel north = new JPanel(new BorderLayout(6, 6)); north.add(form, BorderLayout.CENTER); north.add(controls, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);
        table.setAutoCreateRowSorter(true); table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); add(new JScrollPane(table), BorderLayout.CENTER);
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) selectRow(); });
        add.addActionListener(e -> save(false)); update.addActionListener(e -> save(true)); delete.addActionListener(e -> delete()); clear.addActionListener(e -> clearForm());
        refresh.addActionListener(e -> { refreshLookups(); load(); }); find.addActionListener(e -> load()); filtersForReload();
        refreshLookups(); load();
    }

    private JComponent editor(String column) {
        if (column.equals("project_id")) return choiceBox("projects", true);
        if (column.equals("assigned_worker_id")) return choiceBox("workers", true);
        String[] options = switch (column) {
            case "status" -> spec == RecordDAO.PROJECT ? new String[]{"PLANNED","ONGOING","COMPLETED","ON_HOLD"} : spec == RecordDAO.TASK ? new String[]{"PENDING","IN_PROGRESS","COMPLETED"} : new String[]{"OPEN","IN_PROGRESS","RESOLVED"};
            case "priority" -> new String[]{"LOW","MEDIUM","HIGH"};
            case "availability_status" -> new String[]{"AVAILABLE","BUSY","UNAVAILABLE"};
            default -> null;
        };
        if (options != null) return new JComboBox<>(options);
        return new JTextField();
    }

    private JComboBox<RecordDAO.Choice> choiceBox(String tableName, boolean nullable) {
        JComboBox<RecordDAO.Choice> box = new JComboBox<>();
        if (nullable) box.addItem(new RecordDAO.Choice("", "Unassigned"));
        try { for (RecordDAO.Choice c : dao.choices(tableName)) box.addItem(c); }
        catch (Exception ex) { SwingUtilities.invokeLater(() -> error(ex)); }
        return box;
    }

    public void refreshLookups() {
        loadingLookups=true;
        for (JComponent editor : editors) if (editor instanceof JComboBox<?> raw) {
            @SuppressWarnings("unchecked") JComboBox<RecordDAO.Choice> box=(JComboBox<RecordDAO.Choice>)raw;
            String column=spec.columns()[Arrays.asList(editors).indexOf(editor)];
            if(column.equals("project_id")||column.equals("assigned_worker_id")){box.removeAllItems();box.addItem(new RecordDAO.Choice("","Unassigned"));try{for(RecordDAO.Choice c:dao.choices(column.equals("project_id")?"projects":"workers"))box.addItem(c);}catch(Exception ex){}}
        }
        for (int i=0;i<filters.length;i++) { JComboBox<RecordDAO.Choice> box=filters[i];String col=spec.filterColumns()[i];Object previous=box.getSelectedItem();box.removeAllItems();box.addItem(new RecordDAO.Choice("","All"));populateFilter(box,col);if(previous instanceof RecordDAO.Choice old)for(int j=0;j<box.getItemCount();j++)if(box.getItemAt(j).id().equals(old.id()))box.setSelectedIndex(j); }
        loadingLookups=false;
    }
    private void populateFilter(JComboBox<RecordDAO.Choice> box,String col) {
        if(col.equals("project_id")||col.equals("assigned_worker_id")){try{for(RecordDAO.Choice c:dao.choices(col.equals("project_id")?"projects":"workers"))box.addItem(c);}catch(Exception ignored){}}
        else if(col.equals("role")){try{for(String value:dao.distinct("workers","role"))box.addItem(new RecordDAO.Choice(value,value));}catch(Exception ignored){}}
        else {String[] values=switch(col){case "status"->spec==RecordDAO.PROJECT?new String[]{"PLANNED","ONGOING","COMPLETED","ON_HOLD"}:spec==RecordDAO.TASK?new String[]{"PENDING","IN_PROGRESS","COMPLETED"}:new String[]{"OPEN","IN_PROGRESS","RESOLVED"};case "priority"->new String[]{"LOW","MEDIUM","HIGH"};case "availability_status"->new String[]{"AVAILABLE","BUSY","UNAVAILABLE"};default->new String[0];};for(String value:values)box.addItem(new RecordDAO.Choice(value,value));}
    }
    private void filtersForReload() { search.addActionListener(e -> load()); }
    private String valueAt(JComponent c) {
        if (c instanceof JComboBox<?> combo) { Object item = combo.getSelectedItem(); return item instanceof RecordDAO.Choice choice ? choice.id() : Objects.toString(item, ""); }
        return ((JTextField)c).getText().trim();
    }
    private void setValue(JComponent c, Object raw) {
        String value = raw == null ? "" : raw.toString();
        if (c instanceof JComboBox<?> combo) {
            for (int i=0;i<combo.getItemCount();i++) { Object item=combo.getItemAt(i); if(item instanceof RecordDAO.Choice ch && ch.id().equals(value) || Objects.toString(item, "").equals(value)) { combo.setSelectedIndex(i); return; } }
        } else ((JTextField)c).setText(value);
    }
    private void load() {
        try {
            Map<String,String> selectedFilters = new LinkedHashMap<>();
            for (int i=0;i<filters.length;i++) { Object item=filters[i].getSelectedItem(); if(item instanceof RecordDAO.Choice ch && !ch.id().isBlank()) selectedFilters.put(spec.filterColumns()[i],ch.id()); }
            var rows=dao.all(spec,search.getText(),selectedFilters);
            String[] cols=rows.isEmpty()?new String[]{spec.id()}:rows.get(0).keySet().toArray(String[]::new);
            DefaultTableModel model=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
            for(var row:rows) model.addRow(row.values().toArray()); table.setModel(model);
        } catch(Exception ex) { error(ex); }
    }
    private void save(boolean updating) {
        try {
            if(updating&&selected==null) throw new IllegalArgumentException("Select a row first.");
            List<String> values=new ArrayList<>(); for(JComponent editor:editors) values.add(valueAt(editor)); validateForm(values);
            dao.save(spec,updating?selected:null,values); JOptionPane.showMessageDialog(this,"Record saved."); clearForm(); load();
        } catch(Exception ex) { error(ex); }
    }
    private void validateForm(List<String> v) {
        if(spec==RecordDAO.PROJECT){InputValidator.required(v.get(0),"Project name");InputValidator.required(v.get(1),"Location");InputValidator.progress(v.get(7));InputValidator.nonNegativeMoney(v.get(5));InputValidator.orderedDates(v.get(3),v.get(4),"End date");}
        if(spec==RecordDAO.WORKER){InputValidator.required(v.get(0),"Full name");InputValidator.required(v.get(1),"Role");InputValidator.email(v.get(3));InputValidator.phone(v.get(2));}
        if(spec==RecordDAO.TASK){InputValidator.required(v.get(0),"Task name");InputValidator.required(v.get(1),"Project");InputValidator.progress(v.get(6));InputValidator.orderedDates(v.get(3),v.get(4),"Deadline");if("COMPLETED".equals(v.get(5)))v.set(6,"100");if("100".equals(v.get(6)))v.set(5,"COMPLETED");}
        if(spec==RecordDAO.ISSUE){InputValidator.required(v.get(0),"Project");InputValidator.required(v.get(1),"Issue title");}
    }
    private void selectRow() {
        try { int r=table.convertRowIndexToModel(table.getSelectedRow()); Map<String,Object> row=new LinkedHashMap<>();for(int c=0;c<table.getModel().getColumnCount();c++)row.put(table.getModel().getColumnName(c),table.getModel().getValueAt(r,c));selected=Integer.parseInt(row.get(spec.id()).toString());for(int i=0;i<editors.length;i++)setValue(editors[i],row.get(spec.columns()[i])); }
        catch(Exception ex){error(ex);}
    }
    private void delete(){if(selected==null){JOptionPane.showMessageDialog(this,"Select a row first.");return;}if(JOptionPane.showConfirmDialog(this,"Delete selected record?","Confirm",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)try{dao.delete(spec,selected);clearForm();load();}catch(Exception ex){JOptionPane.showMessageDialog(this,"Delete failed; related records may still reference it. "+ex.getMessage());}}
    private void clearForm(){selected=null;for(int i=0;i<editors.length;i++){JComponent c=editors[i];if(c instanceof JTextField f)f.setText("progress_percentage".equals(spec.columns()[i])?"0":"");else if(c instanceof JComboBox<?> box)box.setSelectedIndex(0);}table.clearSelection();}
    private void error(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"PropVerse",JOptionPane.ERROR_MESSAGE);}
}
