import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Diálogo para crear, editar, guardar y cargar escenarios
 */
public class ScenarioEditorDialog extends JDialog {
    private DemoPanel demoPanel;
    private JComboBox<String> scenarioSelector;
    private JButton saveButton;
    private JButton loadButton;
    private JButton deleteButton;
    private JTextField nameField;
    private JLabel statusLabel;
    
    private static final String SCENARIOS_DIR = "scenarios";
    
    public ScenarioEditorDialog(Frame parent, DemoPanel panel) {
        super(parent, "Editor de Escenarios", true);
        this.demoPanel = panel;
        
        setLayout(new BorderLayout(10, 10));
        
        // Panel superior - Información
        JPanel topPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel infoLabel = new JLabel("<html><b>Editor de Escenarios de Minecraft A*</b><br>" +
                                      "Edita el mapa, luego guarda o carga escenarios</html>");
        topPanel.add(infoLabel);
        
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.add(new JLabel("Nombre del escenario:"));
        nameField = new JTextField(20);
        namePanel.add(nameField);
        topPanel.add(namePanel);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Panel central - Herramientas de edición
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Selector de terreno
        JPanel terrainPanel = new JPanel();
        terrainPanel.setBorder(BorderFactory.createTitledBorder("Herramienta de Pincel"));
        terrainPanel.setLayout(new GridLayout(4, 2, 5, 5));
        
        ButtonGroup terrainGroup = new ButtonGroup();
        
        addTerrainButton(terrainPanel, terrainGroup, "Normal", TerrainType.NORMAL);
        addTerrainButton(terrainPanel, terrainGroup, "Agua (8, contagia)", TerrainType.WATER);
        addTerrainButton(terrainPanel, terrainGroup, "Cactus (8, contagia)", TerrainType.CACTUS);
        addTerrainButton(terrainPanel, terrainGroup, "Miel (8)", TerrainType.HONEY);
        addTerrainButton(terrainPanel, terrainGroup, "Magma (16, contagia, no caminable)", TerrainType.MAGMA);
        addTerrainButton(terrainPanel, terrainGroup, "Muro (bloqueado)", TerrainType.SOLID);
        
        JRadioButton startButton = new JRadioButton("Inicio");
        JRadioButton goalButton = new JRadioButton("Meta");
        terrainGroup.add(startButton);
        terrainGroup.add(goalButton);
        terrainPanel.add(startButton);
        terrainPanel.add(goalButton);
        
        centerPanel.add(terrainPanel);
        
        // Instrucciones
        JPanel instructionsPanel = new JPanel();
        instructionsPanel.setBorder(BorderFactory.createTitledBorder("Instrucciones"));
        JTextArea instructions = new JTextArea(6, 30);
        instructions.setEditable(false);
        instructions.setLineWrap(true);
        instructions.setWrapStyleWord(true);
        instructions.setText(
            "1. Selecciona una herramienta arriba\n" +
            "2. Haz clic en los nodos del mapa para aplicar el terreno\n" +
            "3. Para mover inicio/meta: selecciona 'Inicio' o 'Meta' y haz clic\n" +
            "4. Los bloques que contagian afectarán a sus 8 vecinos\n" +
            "5. No hay doble propagación - cada bloque se afecta solo una vez\n" +
            "6. Guarda el escenario con un nombre\n" +
            "7. Carga escenarios guardados desde la lista"
        );
        instructionsPanel.add(new JScrollPane(instructions));
        centerPanel.add(instructionsPanel);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Panel inferior - Guardar/Cargar
        JPanel bottomPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel loadPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        loadPanel.add(new JLabel("Escenarios guardados:"));
        scenarioSelector = new JComboBox<>();
        refreshScenarioList();
        loadPanel.add(scenarioSelector);
        bottomPanel.add(loadPanel);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        saveButton = new JButton("Guardar Escenario");
        loadButton = new JButton("Cargar Escenario");
        deleteButton = new JButton("Eliminar Escenario");
        JButton closeButton = new JButton("Cerrar");
        
        saveButton.addActionListener(e -> saveScenario());
        loadButton.addActionListener(e -> loadScenario());
        deleteButton.addActionListener(e -> deleteScenario());
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);
        bottomPanel.add(buttonPanel);
        
        statusLabel = new JLabel(" ");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        bottomPanel.add(statusLabel);
        
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Configurar el modo de edición en el panel
        demoPanel.setEditMode(true, terrainGroup);
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void addTerrainButton(JPanel panel, ButtonGroup group, String text, TerrainType type) {
        JRadioButton button = new JRadioButton(text);
        button.setBackground(type.getColor());
        if (type.getCost() < 5) {
            button.setForeground(Color.BLACK);
        } else {
            button.setForeground(Color.WHITE);
        }
        button.putClientProperty("terrainType", type);
        group.add(button);
        panel.add(button);
        
        if (type == TerrainType.NORMAL) {
            button.setSelected(true);
        }
    }
    
    private void refreshScenarioList() {
        scenarioSelector.removeAllItems();
        List<String> scenarios = Scenario.listScenarios(SCENARIOS_DIR);
        for (String scenario : scenarios) {
            scenarioSelector.addItem(scenario);
        }
    }
    
    private void saveScenario() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            statusLabel.setText("Error: Introduce un nombre para el escenario");
            statusLabel.setForeground(Color.RED);
            return;
        }
        
        try {
            File dir = new File(SCENARIOS_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            String filename = SCENARIOS_DIR + File.separator + name + ".scenario";
            Scenario scenario = demoPanel.exportScenario(name);
            scenario.saveToFile(filename);
            
            statusLabel.setText("Escenario guardado: " + name);
            statusLabel.setForeground(new Color(0, 128, 0));
            refreshScenarioList();
            scenarioSelector.setSelectedItem(name);
        } catch (IOException ex) {
            statusLabel.setText("Error al guardar: " + ex.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }
    
    private void loadScenario() {
        String name = (String) scenarioSelector.getSelectedItem();
        if (name == null) {
            statusLabel.setText("Error: Selecciona un escenario para cargar");
            statusLabel.setForeground(Color.RED);
            return;
        }
        
        try {
            String filename = SCENARIOS_DIR + File.separator + name + ".scenario";
            Scenario scenario = Scenario.loadFromFile(filename);
            demoPanel.loadScenario(scenario);
            nameField.setText(scenario.name);
            
            statusLabel.setText("Escenario cargado: " + name);
            statusLabel.setForeground(new Color(0, 128, 0));
        } catch (IOException ex) {
            statusLabel.setText("Error al cargar: " + ex.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }
    
    private void deleteScenario() {
        String name = (String) scenarioSelector.getSelectedItem();
        if (name == null) {
            statusLabel.setText("Error: Selecciona un escenario para eliminar");
            statusLabel.setForeground(Color.RED);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Seguro que quieres eliminar el escenario '" + name + "'?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            String filename = SCENARIOS_DIR + File.separator + name + ".scenario";
            File file = new File(filename);
            if (file.delete()) {
                statusLabel.setText("Escenario eliminado: " + name);
                statusLabel.setForeground(new Color(0, 128, 0));
                refreshScenarioList();
            } else {
                statusLabel.setText("Error al eliminar el archivo");
                statusLabel.setForeground(Color.RED);
            }
        }
    }
    
    @Override
    public void dispose() {
        demoPanel.setEditMode(false, null);
        super.dispose();
    }
}
