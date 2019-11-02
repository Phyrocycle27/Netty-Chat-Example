import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class Encryption {

    private KeyPair clientKeyPair;
    private SecretKeySpec clientAesKey;

    public boolean isKeySet() {
        return clientAesKey != null;
    }

    public byte[] encode(String s) {
        byte[] result = new byte[0];
        try {
            Cipher serverCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            serverCipher.init(Cipher.ENCRYPT_MODE, clientAesKey);
            byte[] data = serverCipher.doFinal(s.getBytes());
            // Передаёт клиенту параметры, с которыми выполнялась шифровка
            byte[] params = serverCipher.getParameters().getEncoded();

            byte[] tmp = new byte[params.length + data.length];
            System.arraycopy(params, 0, tmp, 0, params.length);
            System.arraycopy(data, 0, tmp, params.length, data.length);

            result = tmp;

        } catch (IOException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException |
                NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String decode(byte[] data, byte[] params) {
        String s = "";
        try {
            AlgorithmParameters aesParams = AlgorithmParameters.getInstance("AES");
            aesParams.init(params);
            Cipher clientCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            clientCipher.init(Cipher.DECRYPT_MODE, clientAesKey, aesParams);
            byte[] recovered = clientCipher.doFinal(data);
            s = new String(recovered);

        } catch (NoSuchAlgorithmException | IllegalBlockSizeException | IOException | NoSuchPaddingException
                | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException e) {
            e.printStackTrace();
        }
        return s;
    }

    public byte[] getPublicKey() {
        byte[] clientPubKeyEncoded = new byte[0];
        try {
            KeyPairGenerator clientGen = KeyPairGenerator.getInstance("DH");
            clientKeyPair = clientGen.generateKeyPair();
            clientPubKeyEncoded = clientKeyPair.getPublic().getEncoded();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return clientPubKeyEncoded;
    }

    public void createSharedKey(byte[] serverPubKeyEncoded) {
        try {
            KeyAgreement clientKeyAgree = KeyAgreement.getInstance("DH");
            clientKeyAgree.init(clientKeyPair.getPrivate());
            // Клиент на основе ключа сервера и своего private key создаёт общий shared ключ
            KeyFactory clientKeyFactory = KeyFactory.getInstance("DH");
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(serverPubKeyEncoded);
            PublicKey serverPubKey = clientKeyFactory.generatePublic(x509KeySpec);
            // **************
            clientKeyAgree.doPhase(serverPubKey, true);
            byte[] clientSharedSecret = clientKeyAgree.generateSecret();
            clientAesKey = new SecretKeySpec(clientSharedSecret, 0, 16, "AES");

        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }
}
