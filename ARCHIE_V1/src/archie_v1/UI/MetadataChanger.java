//License
package archie_v1.UI;

import archie_v1.Dataset;
import archie_v1.fileHelpers.FileHelper;
import archie_v1.fileHelpers.FolderHelper;
import archie_v1.outputFormats.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

public class MetadataChanger extends JSplitPane implements TreeSelectionListener, ActionListener {

    public enum SaveType {
        Islandora, Dans, Archie
    }

    Path mainDirectory;
    JTree UITree;
    Dataset dataset;

    public MetadataChanger(Dataset dataset) {
        mainDirectory = dataset.mainDirectory;
        this.dataset = dataset;

        createUI();
    }

    public boolean Save(SaveType st, Path outputPath) throws IOException {
        outputAbstract output;
        if (st == SaveType.Islandora) {
            output = new outputIslandora();
        } else if (st == SaveType.Dans) {
            output = new outputDANS(dataset);
        } else if (st == SaveType.Archie){
            output = new outputArchie(dataset);
        } else {
            JOptionPane.showMessageDialog(this, "The chosen save mode has not been implemented yet.");
            return false;
        }
        output.Save(outputPath.toString(), dataset.files, this);

        return true;
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) UITree.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }
        Path nodeInfo = (Path) node.getUserObject();

        for (FileHelper fh : dataset.files) {
            if (nodeInfo.equals(fh.filePath)) {
                updateChangerPane(fh);
                return;
            }
        }

        WelcomeScreen ws = new WelcomeScreen();
        this.setRightComponent(ws);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void resetLeftPane() {
        UITree = new JTree(dataset.fileTree) {
            @Override
            public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Path path = (Path) node.getUserObject();
                return path.toFile().getName();
            }
        };
        UITree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        UITree.addTreeSelectionListener(this);

        JScrollPane fileTreeView = new JScrollPane(UITree);
        this.setLeftComponent(fileTreeView);
    }

    private void createUI() {
        this.orientation = JSplitPane.HORIZONTAL_SPLIT;
        this.setResizeWeight(0.2);

        resetLeftPane();

        for (FileHelper fh : dataset.files) {
            if (dataset.fileTree.getRoot().toString().equals(fh.filePath.toString())) {
                this.setRightComponent(new MetadataChangerPane(fh, this));
                return;
            }
        }

        WelcomeScreen ws = new WelcomeScreen();
        this.setRightComponent(ws);
    }

    private void updateChangerPane(FileHelper fileHelper) {
        if (MetadataChangerPane.class.isInstance(getRightComponent())) {
            boolean isFolderHelper = FolderHelper.class.isInstance(((MetadataChangerPane) this.getRightComponent()).fileHelper);
            boolean isRoot = false;
            if(isFolderHelper)
                isRoot = ((FolderHelper)((MetadataChangerPane) this.getRightComponent()).fileHelper).root;
            if(!isRoot)
                ((MetadataChangerPane) this.getRightComponent()).saveToFileHelper(isFolderHelper);
        }
        this.setRightComponent(new MetadataChangerPane(fileHelper, this));
    }
}
