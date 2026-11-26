import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class SchedulerGUI extends JFrame {

    static class GanttPanel extends JPanel {
        static final int UNIT = 32, TOP = 20, H = 46;
        List<Result.Seg> segs = new ArrayList<>();
        static final Color[] COLORS = {
                new Color(0x4C9BE8), new Color(0x57C785), new Color(0xF2A541), new Color(0xE0636B),
                new Color(0x9B7EDE), new Color(0x4DC0C0), new Color(0xE58AC0), new Color(0xA3B84B)};

        void setSegs(List<Result.Seg> s) {
            segs = s;
            int end = s.isEmpty() ? 0 : s.get(s.size() - 1).end;
            setPreferredSize(new Dimension(end * UNIT + 40, TOP + H + 40));
            revalidate();
            repaint();
        }

        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(getFont().deriveFont(Font.BOLD, 12f));
            FontMetrics fm = g.getFontMetrics();
            for (Result.Seg s : segs) {
                int x = 10 + s.start * UNIT, w = (s.end - s.start) * UNIT;
                Color c;
                if (s.label.equals("IDLE")) c = new Color(0xCCCCCC);
                else if (s.label.equals("CS")) c = new Color(0x777777);
                else c = COLORS[(Integer.parseInt(s.label.substring(1)) - 1) % COLORS.length];
                g.setColor(c);
                g.fillRect(x, TOP, w, H);
                g.setColor(Color.DARK_GRAY);
                g.drawRect(x, TOP, w, H);
                g.setColor(s.label.equals("IDLE") ? Color.DARK_GRAY : Color.WHITE);
                String t = w < fm.stringWidth(s.label) + 4 ? "" : s.label;
                g.drawString(t, x + (w - fm.stringWidth(t)) / 2, TOP + H / 2 + 5);
                g.setColor(Color.GRAY);
                g.drawString(String.valueOf(s.start), x - fm.stringWidth(String.valueOf(s.start)) / 2, TOP + H + 18);
            }
            if (!segs.isEmpty()) {
                int e = segs.get(segs.size() - 1).end;
                g.drawString(String.valueOf(e), 10 + e * UNIT - fm.stringWidth(String.valueOf(e)) / 2, TOP + H + 18);
            }
        }
    }

    private final DefaultTableModel inputModel =
            new DefaultTableModel(new Object[]{"Process", "Arrival", "Burst", "Priority"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return c != 0; }
            };
    private final DefaultTableModel resultModel =
            new DefaultTableModel(new Object[]{"Process", "Finish", "Turnaround", "Waiting", "Response"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
    private final JComboBox<String> algo = new JComboBox<>(new String[]{
            "SRTF  (Shortest Remaining Time First)", "ACS  (Adaptive Container Scheduler)"});
    private final JSpinner csSpin = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    private final JSpinner qSpin = new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));
    private final JSpinner agingSpin = new JSpinner(new SpinnerNumberModel(4, 1, 100, 1));
    private final GanttPanel gantt = new GanttPanel();
    private final JTextArea logArea = new JTextArea();
    private final JLabel avgLabel = new JLabel(" ");

    public SchedulerGUI() {
        super("CPU Scheduler Simulator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JTable inTable = new JTable(inputModel);
        inTable.setRowHeight(24);
        JPanel left = new JPanel(new BorderLayout(4, 4));
        left.setBorder(BorderFactory.createTitledBorder("Processes"));
        left.add(new JScrollPane(inTable), BorderLayout.CENTER);

        JButton add = new JButton("Add");
        JButton remove = new JButton("Remove");
        JButton clear = new JButton("Clear");
        add.addActionListener(e -> addRow(0, 1, 1));
        remove.addActionListener(e -> {
            if (inTable.isEditing()) inTable.getCellEditor().stopCellEditing();
            int r = inTable.getSelectedRow();
            if (r >= 0) { inputModel.removeRow(r); renumber(); }
        });
        clear.addActionListener(e -> inputModel.setRowCount(0));
        JPanel btns = new JPanel(new GridLayout(1, 3, 4, 0));
        btns.add(add); btns.add(remove); btns.add(clear);
        left.add(btns, BorderLayout.SOUTH);
        left.setPreferredSize(new Dimension(300, 0));

        JPanel settings = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        settings.setBorder(BorderFactory.createTitledBorder("Settings"));
        settings.add(new JLabel("Algorithm:")); settings.add(algo);
        settings.add(new JLabel("Context switch:")); settings.add(csSpin);
        JLabel ql = new JLabel("Quantum:"), al = new JLabel("Aging:");
        settings.add(ql); settings.add(qSpin);
        settings.add(al); settings.add(agingSpin);
        JButton run = new JButton("Run");
        run.setFont(run.getFont().deriveFont(Font.BOLD));
        settings.add(run);
        algo.addActionListener(e -> {
            boolean a = algo.getSelectedIndex() == 1;
            ql.setEnabled(a); qSpin.setEnabled(a); al.setEnabled(a); agingSpin.setEnabled(a);
        });
        ql.setEnabled(false); qSpin.setEnabled(false); al.setEnabled(false); agingSpin.setEnabled(false);
        run.addActionListener(e -> runSimulation(inTable));

        JScrollPane ganttScroll = new JScrollPane(gantt);
        ganttScroll.setBorder(BorderFactory.createTitledBorder("Gantt chart  (dark grey = context switch, light grey = idle)"));
        ganttScroll.setPreferredSize(new Dimension(0, 130));

        JTable resTable = new JTable(resultModel);
        resTable.setRowHeight(22);
        JPanel resPanel = new JPanel(new BorderLayout());
        resPanel.setBorder(BorderFactory.createTitledBorder("Results"));
        resPanel.add(new JScrollPane(resTable), BorderLayout.CENTER);
        avgLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        resPanel.add(avgLabel, BorderLayout.SOUTH);

        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Timeline log"));

        JSplitPane bottom = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, resPanel, logScroll);
        bottom.setResizeWeight(0.45);

        JPanel center = new JPanel(new BorderLayout(6, 6));
        center.add(ganttScroll, BorderLayout.NORTH);
        center.add(bottom, BorderLayout.CENTER);

        add(settings, BorderLayout.NORTH);
        add(left, BorderLayout.WEST);
        add(center, BorderLayout.CENTER);

        addRow(0, 5, 4);
        addRow(1, 6, 2);
        addRow(4, 4, 3);

        setSize(1050, 650);
        setLocationRelativeTo(null);
    }

    private void addRow(int arrival, int burst, int priority) {
        inputModel.addRow(new Object[]{"P" + (inputModel.getRowCount() + 1), arrival, burst, priority});
    }

    private void renumber() {
        for (int i = 0; i < inputModel.getRowCount(); i++) inputModel.setValueAt("P" + (i + 1), i, 0);
    }

    private void runSimulation(JTable inTable) {
        if (inTable.isEditing()) inTable.getCellEditor().stopCellEditing();
        int n = inputModel.getRowCount();
        if (n == 0) { error("Add at least one process."); return; }

        Process[] ps = new Process[n];
        try {
            for (int i = 0; i < n; i++) {
                int a = Integer.parseInt(inputModel.getValueAt(i, 1).toString().trim());
                int b = Integer.parseInt(inputModel.getValueAt(i, 2).toString().trim());
                int pr = Integer.parseInt(inputModel.getValueAt(i, 3).toString().trim());
                if (a < 0 || b <= 0) { error("P" + (i + 1) + ": arrival must be >= 0 and burst > 0."); return; }
                ps[i] = new Process(i + 1, a, b, pr);
            }
        } catch (NumberFormatException ex) {
            error("All arrival, burst and priority values must be whole numbers.");
            return;
        }

        int cs = (Integer) csSpin.getValue();
        Result r;
        if (algo.getSelectedIndex() == 0)
            r = Main.run(new ArrayList<>(Arrays.asList(ps)), cs);
        else
            r = ACSShort.run(ps, (Integer) qSpin.getValue(), cs, (Integer) agingSpin.getValue());

        gantt.setSegs(r.segments);
        logArea.setText(String.join("\n", r.log));
        logArea.setCaretPosition(0);
        avgLabel.setText(String.format("Average Turnaround = %.2f     Average Waiting = %.2f",
                r.avgTurnaround, r.avgWaiting));

        Process[] byId = r.processes.clone();
        Arrays.sort(byId, Comparator.comparingInt(p -> p.id));
        resultModel.setRowCount(0);
        for (Process p : byId)
            resultModel.addRow(new Object[]{"P" + p.id, p.completion, p.turnaroundTime, p.waitingTime, p.responseTime});
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Input error", JOptionPane.ERROR_MESSAGE);
    }
}