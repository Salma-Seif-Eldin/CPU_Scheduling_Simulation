import java.util.*;

public class ACSShort {

    static Result run(Process[] ps, int q, int cs, int aging) {
        Result r = new Result();
        Arrays.sort(ps, Comparator.comparingInt((Process p) -> p.arrival));
        r.processes = ps;
        int n = ps.length, time = 0, done = 0;
        int[] demotedAt = new int[n];
        Arrays.fill(demotedAt, -1);
        Process last = null;

        while (done < n) {
            int pick = -1;
            for (int i = 0; i < n && pick < 0; i++)
                if (ps[i].arrival <= time && demotedAt[i] < 0 && ps[i].remaining > 0) pick = i;

            int best = Integer.MAX_VALUE;
            if (pick < 0)
                for (int i = 0; i < n; i++) {
                    if (demotedAt[i] < 0 || ps[i].remaining == 0) continue;
                    int pr = ps[i].priority - (time - demotedAt[i]) / aging;
                    if (pr < best || (pr == best && demotedAt[i] < demotedAt[pick])) { best = pr; pick = i; }
                }

            Process p = (pick < 0) ? null : ps[pick];
            int switchTime = (p != null && last != null && last != p) ? cs : 0;
            int runTime = (p == null) ? 0
                        : (demotedAt[pick] >= 0) ? p.remaining
                        : Math.min(q, p.remaining);
            int seconds = (p == null) ? 1 : switchTime + runTime;

            for (int s = 0; s < seconds; s++) {
                String what = (p == null) ? "IDLE"
                            : (s < switchTime) ? "Context switch saves P" + last.id + " and loads P" + p.id
                            : "P" + p.id;
                r.unit(time, p == null ? "IDLE" : (s < switchTime ? "CS" : "P" + p.id));

                String[] label = new String[n];
                List<String> tier1 = new ArrayList<>(), tier2 = new ArrayList<>();
                List<Integer> inTier2 = new ArrayList<>();
                for (int i = 0; i < n; i++) {
                    Process x = ps[i];
                    if (x.arrival > time || x.remaining == 0 || (x == p && s > 0)) continue;
                    int pri = (demotedAt[i] < 0) ? x.priority : x.priority - (time - demotedAt[i]) / aging;
                    label[i] = "P" + x.id + " (remaining: " + x.remaining + ", Priority: " + pri + ")";
                    if (demotedAt[i] < 0) tier1.add(label[i]); else inTier2.add(i);
                }
                inTier2.sort(Comparator.comparingInt(i -> demotedAt[i]));
                for (int i : inTier2) tier2.add(label[i]);

                r.log(String.format("%3d | %s", time, what));
                r.log("      Tier 1: " + (tier1.isEmpty() ? "[Empty]" : tier1));
                r.log("      Tier 2: " + (tier2.isEmpty() ? "[Empty]" : tier2));

                if (p != null && s >= switchTime) {
                    if (p.firstResponse < 0) p.firstResponse = time;
                    p.remaining--;
                }
                time++;
            }

            if (p == null) continue;
            last = p;
            if (p.remaining == 0) { p.completion = time; done++; }
            else demotedAt[pick] = time;
        }

        r.log(String.format("%3d | done", time));
        r.log("      Tier 1: [Empty]");
        r.log("      Tier 2: [Empty]");

        double totalTurnaround = 0, totalWaiting = 0;
        for (Process p : ps) {
            p.turnaroundTime = p.completion - p.arrival;
            p.waitingTime = p.turnaroundTime - p.burst;
            p.responseTime = p.firstResponse - p.arrival;
            totalTurnaround += p.turnaroundTime;
            totalWaiting += p.waitingTime;
        }
        r.avgTurnaround = totalTurnaround / n;
        r.avgWaiting = totalWaiting / n;
        return r;
    }
}