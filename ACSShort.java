import java.util.*;

public class ACSShort {

    static void run(Process[] ps, int q, int cs, int aging) {
        Arrays.sort(ps, Comparator.comparingInt((Process p) -> p.arrival));
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

                System.out.printf("%3d | %s%n", time, what);
                System.out.printf("      Tier 1: %s%n      Tier 2: %s%n",
                        tier1.isEmpty() ? "[Empty]" : tier1, tier2.isEmpty() ? "[Empty]" : tier2);

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

        System.out.printf("%3d | done%n      Tier 1: [Empty]%n      Tier 2: [Empty]%n", time);
        double totalTurnaround = 0, totalWaiting = 0;
        System.out.println("\nProcess | Finish | Turnaround | Waiting | Response");
        for (Process p : ps) {
            p.turnaroundTime = p.completion - p.arrival;
            p.waitingTime = p.turnaroundTime - p.burst;
            p.responseTime = p.firstResponse - p.arrival;
            totalTurnaround += p.turnaroundTime;
            totalWaiting += p.waitingTime;
            System.out.printf("P%-6d | %6d | %10d | %7d | %8d%n", p.id, p.completion, p.turnaroundTime, p.waitingTime, p.responseTime);
        }
        System.out.printf("Average Turnaround: %.2f   Average Waiting: %.2f%n", totalTurnaround / n, totalWaiting / n);
    }

    public static void main(String[] args) {
        System.out.println("=== Test Case 1 (Q=3, CS=0) ===");
        run(new Process[]{ new Process(1, 0, 4, 2), new Process(2, 1, 7, 3), new Process(3, 3, 2, 1) }, 3, 0, 4);

        System.out.println("\n=== Test Case 2 (Q=3, CS=1) ===");
        run(new Process[]{ new Process(1, 0, 5, 4), new Process(2, 1, 6, 2), new Process(3, 4, 4, 3) }, 3, 1, 4);
    }
}