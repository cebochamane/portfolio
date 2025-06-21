import java.util.ArrayList;
import java.util.Date;

public class Stina {
    public String hash;
    public String prevHash;
    private String data;
    private long timeStamp;
    private int nonce;
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>();

    public Stina(String data, String prevHash) {
        this.data = data;
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
        String target = new String(new char[difficulty]).replace('\0', '0'); // Create a string with difficulty * "0"
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!!! : " + hash);
    }

}