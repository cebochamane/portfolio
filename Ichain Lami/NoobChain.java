import java.util.ArrayList;
import com.google.gson.GsonBuilder;

public class NoobChain {

    public static ArrayList<Stina> blockchain = new ArrayList<Stina>();
    public static int difficulty = 5;

    public static void main(String[] args) {
        // add our blocks to the blockchain ArrayList:
        Stina genesisBlock = new Stina("Hi im the first block", "0");
        genesisBlock.mineBlock(difficulty);
        blockchain.add(genesisBlock);
        System.out.println("Hash for block 1 : " + genesisBlock.hash);

        Stina secondBlock = new Stina("Yo im the second block", genesisBlock.hash);
        secondBlock.mineBlock(difficulty);
        blockchain.add(secondBlock);
        System.out.println("Hash for block 2 : " + secondBlock.hash);

        Stina thirdBlock = new Stina("Hey im the third block", secondBlock.hash);
        thirdBlock.mineBlock(difficulty);
        blockchain.add(thirdBlock);
        System.out.println("Hash for block 3 : " + thirdBlock.hash);

        String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
        System.out.println("\nBlockchain:");
        System.out.println(blockchainJson);
    }

    public static boolean validChain() {
        Stina currentBlock;
        Stina prevBlock;

        String hashTarget = new String(new char[difficulty]).replace('\0', '0');

        // loop through blockchain to check hashes:
        for (int i = 1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            prevBlock = blockchain.get(i - 1);
            // compare registered hash and calculated hash:
            if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
                System.out.println("Current Hashes not equal");
                return false;
            }
            // compare previous hash and registered previous hash
            if (!prevBlock.hash.equals(currentBlock.prevHash)) {
                System.out.println("Previous Hashes not equal");
                return false;
            }
            // check if hash is solved
            if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
                System.out.println("This block hasn't been mined");
                return false;
            }
        }
        return true;
    }

}
