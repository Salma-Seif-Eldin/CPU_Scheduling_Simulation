import java.util.*;

public class Result {

    static class Seg {
        int start, end;
        String label;
        Seg(int start, int end, String label) { this.start = start; this.end = end; this.label = label; }
    }

    List<Seg> segments = new ArrayList<>();
    List<String> log = new ArrayList<>();
    Process[] processes;
    double avgTurnaround;
    double avgWaiting;

    void unit(int t, String label) {
        if (!segments.isEmpty()) {
            Seg last = segments.get(segments.size() - 1);
            if (last.end == t && last.label.equals(label)) { last.end = t + 1; return; }
        }
        segments.add(new Seg(t, t + 1, label));
    }

    void log(String line) { log.add(line); }
}