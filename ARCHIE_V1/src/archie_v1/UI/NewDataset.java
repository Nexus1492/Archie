//License
package archie_v1.UI;

import archie_v1.ARCHIE;
import archie_v1.Dataset;
import archie_v1.fileHelpers.FolderHelper;
import archie_v1.fileHelpers.MetadataKey;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 *
 * @author niels
 */
public class NewDataset extends JPanel implements ActionListener, PropertyChangeListener {

    private JButton generate, cancel;
    private MainFrame parent;
    private MetadataChangerFields fields;
    private FolderHelper datasetHelper;
    private String datasetName;
    private Path datasetPath;
    private Dataset dataset;
    private int datasetSize;
    ProgressMonitor pm = new ProgressMonitor(this, "Creating dataset", "", 0, 100);
    SwingWorker task;
    public boolean busy = true;

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("progress")) {
            int progress = (int) evt.getNewValue();
            pm.setProgress(progress);
            pm.setNote("File " + progress + " of " + datasetSize);
            if (task.isDone() || task.isCancelled()) {
                System.out.println("Job's done!");
                pm.close();
                parent.setCursor(null);
            }
        }
    }
    
    public NewDataset(MainFrame parent) {
        this.parent = parent;
        //String path = ARCHIE.getRecentlyGenerated();
        String path = null;
        if(path!=null){
            datasetHelper = new FolderHelper(Paths.get(path), true);
        } else {
            datasetHelper = new FolderHelper(null, true);
        }
        pm.setMillisToDecideToPopup(0);
        pm.setMillisToPopup(0);
        createUI();
    }

    public NewDataset(MainFrame parent, File selectedFile) {
        this.parent = parent;
        pm.setMillisToDecideToPopup(0);
        pm.setMillisToPopup(0);
        dataset = new Dataset(selectedFile);
        createMetadataChanger(true);
    }

    public boolean initializeNewDataset() {
        
        for (Map.Entry<MetadataKey, JComponent> metadataKeyTextEntry : fields.labelText.entrySet()) {
            String value = (metadataKeyTextEntry.getKey().unrestricted) ? ((ArchieTextField) metadataKeyTextEntry.getValue()).getText() : ((JComboBox) metadataKeyTextEntry.getValue()).getSelectedItem().toString();
            if (value != null && !value.equals("")) {
                datasetHelper.setRecord(metadataKeyTextEntry.getKey(), value, false);
            }
        }
        for (AddablePanel addablePanel : fields.addablePanels) {
            datasetHelper.SetAddableRecord(addablePanel.Values, addablePanel.valueArray, true);
        }
        
        datasetName = datasetHelper.metadataMap.get(MetadataKey.DatasetTitle);
        datasetPath = Paths.get(fields.datasetLocationField.getText());

        if (datasetName == null || "".equals(datasetName)) {
            JOptionPane.showMessageDialog(this, "The name of a dataset can not be empty.", "Dataset name", JOptionPane.PLAIN_MESSAGE);
            return false;
        } else if (datasetPath == null || "".equals(datasetPath.toString())) {
            JOptionPane.showMessageDialog(this, "The path of a dataset can not be empty.", "Dataset path", JOptionPane.PLAIN_MESSAGE);
            return false;
        }

        //Start generating the metadata and UI.
        //ARCHIE.setRecentlyGenerated(datasetPath);
        datasetSize = FileUtils.listFilesAndDirs(datasetPath.toFile(), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE).size();
        datasetHelper = new FolderHelper(datasetPath, true);

        //Setting the mainpanel to a progresspanel
        
        dataset = new Dataset(datasetPath, datasetHelper, datasetSize);
        return true;
    }

    private void createUI() {
        this.setLayout(new GridBagLayout());

        //Panel setup
        GridBagConstraints gbc;
        fields = new MetadataChangerFields(datasetHelper, true);

        gbc = new GridBagConstraints(0, 0, 1, 1, 1, .95, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0, 0);
        this.add(fields, gbc);

        //Bottom panel containing generation and cancellation buttons
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        cancel = new JButton("Cancel");
        cancel.addActionListener(this);
        gbc = new GridBagConstraints();
        gbc = new GridBagConstraints(
                1, 0, //GridX, GridY
                1, 1, //GridWidth, GridHeight
                .1, 1, //WeightX, WeightY
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, //Anchor, Fill
                new Insets(0, 0, 0, 0), 0, 0);                      //Insets, IpadX, IpadY
        bottomPanel.add(cancel, gbc);

        JPanel test = new JPanel();

        gbc = new GridBagConstraints();
        gbc = new GridBagConstraints(
                2, 0, //GridX, GridY
                2, 1, //GridWidth, GridHeight
                .5, 1, //WeightX, WeightY
                GridBagConstraints.CENTER, GridBagConstraints.NONE, //Anchor, Fill
                new Insets(0, 0, 0, 0), 0, 0);                              //Insets, IpadX, IpadY
        bottomPanel.add(test, gbc);

        generate = new JButton("Generate");
        generate.addActionListener(this);
        gbc = new GridBagConstraints();
        gbc = new GridBagConstraints(
                4, 0, //GridX, GridY
                1, 1, //GridWidth, GridHeight
                .1, 1, //WeightX, WeightY
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, //Anchor, Fill
                new Insets(0, 0, 0, 0), 0, 0);                              //Insets, IpadX, IpadY
        bottomPanel.add(generate, gbc);

        gbc = new GridBagConstraints(0, 1, 1, 1, 1, .05, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0, 0);
        this.add(bottomPanel, gbc);
    }

    public void createMetadataChanger(boolean open) {
        boolean succes = open;
        if (!open) {
            succes = initializeNewDataset();
        }

        if (!succes) {
            return;
        }

        setCursor(null);

        //Setting the mainpanel to a metadatachanger
        parent.metadatachanger = new MetadataChanger(dataset);
        parent.ChangeMainPanel(parent.metadatachanger);
        parent.export.setEnabled(true);
        parent.saveItem.setEnabled(true);

        //CHECK THIS
        parent.validate();
        parent.pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancel) {
            parent.goToHome();
        } else if (e.getSource() == generate) {
            createMetadataChanger(false);
        }
    }
}
