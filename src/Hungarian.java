import java.util.Arrays;

/**
 * Created by alex on 2/8/15.
 */
public class Hungarian {
    static int N = 3;
    static int[][] cost = {{7,4,3}, {6,8,5}, {9,4,4}};          //cost matrix
    static int n = N, max_match;        //n workers and n jobs
    static int[] lx = new int[N];
    static int[] ly = new int[N];        //labels of X and Y parts
    static int[] xy = new int[N];               //xy[x] - vertex that is matched with x,
    static int[] yx = new int[N];               //yx[y] - vertex that is matched with y
    static boolean S[] = new boolean[N];
    static boolean T[] = new boolean[N];         //sets S and T in algorithm
    static int[] slack = new int[N];            //as in the algorithm description
    static int[] slackx = new int[N];           //slackx[y] such a vertex, that
                                    // l(slackx[y]) + l(y) - w(slackx[y],y) = slack[y]
    static int[] prev = new int[N];

    static void init_labels() {
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                lx[x] = Math.max(lx[x], cost[x][y]);
            }
        }
    }

    static void update_labels() {
        int x, y, delta = Integer.MAX_VALUE;             //init delta as infinity
        for (y = 0; y < n; y++)            //calculate delta using slack
            if (!T[y])
                delta = Math.min(delta, slack[y]);
        for (x = 0; x < n; x++)            //update X labels
            if (S[x]) lx[x] -= delta;
        for (y = 0; y < n; y++)            //update Y labels
            if (T[y]) ly[y] += delta;
        for (y = 0; y < n; y++)            //update slack array
            if (!T[y])
                slack[y] -= delta;
    }

    static void add_to_tree(int x, int prevx) {
        S[x] = true;                    //add x to S
        prev[x] = prevx;                //we need this when augmenting
        for (int y = 0; y < n; y++)    //update slacks, because we add new vertex to S
            if (lx[x] + ly[y] - cost[x][y] < slack[y])
            {
                slack[y] = lx[x] + ly[y] - cost[x][y];
                slackx[y] = x;
            }
    }

    static void augment() {
        if (max_match == n) return;        //check wether matching is already perfect
        int x, y, root = -1;                    //just counters and root vertex
        int[] q = new int[N]; int wr = 0, rd = 0;          //q - queue for bfs, wr,rd - write and read
        //pos in queue
        Arrays.fill(S, false);
        Arrays.fill(T, false);
        Arrays.fill(prev, -1);
        for (x = 0; x < n; x++) {           //finding root of the tree
            if (xy[x] == -1) {
                q[wr++] = root = x;
                prev[x] = -2;
                S[x] = true;
                break;
            }
        }

        for (y = 0; y < n; y++){
            slack[y] = lx[root] + ly[y] - cost[root][y];
            slackx[y] = root;
        }
        while (true) {
            while (rd < wr){
                x = q[rd++];                                                //current vertex from X part
                for (y = 0; y < n; y++) {                                     //iterate through all edges in equality graph
                    if (cost[x][y] == lx[x] + ly[y] && !T[y]) {
                        if (yx[y] == -1) break;                             //an exposed vertex in Y found, so
                        //augmenting path exists!
                        T[y] = true;                                        //else just add y to T,
                        q[wr++] = yx[y];                                    //add vertex yx[y], which is matched
                        //with y, to the queue
                        add_to_tree(yx[y], x);                              //add edges (x,y) and (y,yx[y]) to the tree
                    }
                }
                if (y < n) break;                                           //augmenting path found!
            }
            if (y < n) break;                                               //augmenting path found!

            update_labels();                                                //augmenting path not found, so improve labeling
            wr = rd = 0;
            for (y = 0; y < n; y++) {
                //in this cycle we add edges that were added to the equality graph as a
                //result of improving the labeling, we add edge (slackx[y], y) to the tree if
                //and only if !T[y] &&  slack[y] == 0, also with this edge we add another one
                //(y, yx[y]) or augment the matching, if y was exposed
                if (!T[y] && slack[y] == 0) {
                    if (yx[y] == -1) {
                        x = slackx[y];
                        break;
                    } else {
                        T[y] = true;                                        //else just add y to T,
                        if (!S[yx[y]]) {
                            q[wr++] = yx[y];                                //add vertex yx[y], which is matched with
                            //y, to the queue
                            add_to_tree(yx[y], slackx[y]);                  //and add edges (x,y) and (y,
                            //yx[y]) to the tree
                        }
                    }
                }
            }
            if (y < n) break;                                               //augmenting path found!
        }

        if (y < n) {
            max_match++;                                                    //increment matching
            //in this cycle we inverse edges along augmenting path
            for (int cx = x, cy = y, ty; cx != -2; cx = prev[cx], cy = ty) {
                ty = xy[cx];
                yx[cy] = cx;
                xy[cx] = cy;
            }
            augment();                                                      //recall function, go to step 1 of the algorithm
        }
    }

    public static void main(String[] args){
        int ret = 0;
        max_match = 0;
        Arrays.fill(xy, -1);
        Arrays.fill(yx, -1);
        init_labels();
        augment();
        for (int x = 0; x < n; x++) {
            ret += cost[x][xy[x]];
        }
        System.out.println(ret);
    }
}
