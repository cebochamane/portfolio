import java.util.ArrayList;
import java.util.Date;

public class Stina {
    public String hash;
    public String prevHash;
    private long timeStamp;
    private int nonce;
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>();

    public Stina(String prevHash) {

        this.prevHash = prevHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    // Calculate new hash based on blocks contents
    public String calculateHash() {
        String calculatedhash = StringUtil.turnIntoUnrecognizableGibberish(
                prevHash +
                        Long.toString(timeStamp) +
                        Integer.toString(nonce) +
                        merkleRoot);
        return calculatedhash;
    }

    public void mineBlock(int difficulty) {
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = StringUtil.getDifficultyString(difficulty); // Create a string with difficulty * "0"
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!!! : " + hash);
    }

    // Add transactions to this block
    public boolean addTransaction(Transaction transaction) {
        // process transaction and check if valid, unless block is genesis block then
        // ignore.
        if (transaction == null)
            return false;
        if ((prevHash != "0")) {
            if ((transaction.processTransaction() != true)) {
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Transaction Successfully added to Block");
        return true;
    }

}