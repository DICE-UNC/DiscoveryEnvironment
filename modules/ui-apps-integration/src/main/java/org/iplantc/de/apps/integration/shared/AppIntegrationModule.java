package org.iplantc.de.apps.integration.shared;

/**
 * @author jstroot
 */
public interface AppIntegrationModule {

    interface Ids {
        String APP_EDITOR_VIEW = ".appEditorView";
        String PALETTE = ".pallete";

        String ENV_VARIABLE = ".environmentVariable";
        String FILE_INPUT = ".fileInput";
        String FLAG = ".flag";
        String INTEGER_INPUT = ".integerInput";
        String MULTI_FILE_SELECTOR = ".multiFileSelector";
        String MULTI_LINE_TEXT = ".multiLineText";
        String TEXT = ".text";
        String SINGLE_SELECT = ".singleSelect";
        String TREE_SELECTION = ".treeSelection";
        String INFO = ".info";
        String FOLDER_INPUT = ".folderInput";
        String INTEGER_SELECTION = ".integerSelection";
        String DOUBLE_SELECTION = ".doubleSelection";
        String DOUBLE_INPUT = ".doubleInput";
        String FILE_OUTPUT = ".fileOutput";
        String FOLDER_OUTPUT = ".folderOutput";
        String MULTI_FILE_OUTPUT = ".multiFileOutput";
        String REFERENCE_GENOME = ".referenceGenome";
        String REFERENCE_ANNOTATION = ".referenceAnnotation";
        String REFERENCE_SEQUENCE = ".referenceSequence";

        String GROUP = ".group";
    }
}