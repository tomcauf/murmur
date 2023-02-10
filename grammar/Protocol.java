package murmur.grammar;

import java.security.cert.CRL;

public class Protocol {
    // Définitions standards :
    private final String CHIFFRE = "[0-9]";
    private final String LETTRE = "[a-zA-Z]";
    private final String LETTRE_CHIFFRE = String.format("(%s|%s)",LETTRE,CHIFFRE);
    private final String CARACTERE_IMPRIMABLE = "[\\x20-\\xff]";
    private final String CRLF = "\\x0d\\x0a"; // OU => \\r\\n
    private final String SYMBOLE = "[\\x21-\\x2f\\x3a-\\x40\\x5b-\\x60]";
    private final String ESP = "\\x20";
    private final String DOMAINE = String.format("(%s|%s){5,200}",LETTRE_CHIFFRE,"\\x2e");
    private final String PORT = String.format("%s{1,5}",CHIFFRE);
    private final String ROUND = String.format("%s{2}",CHIFFRE);
    private final String BCRYPT_HASH = String.format("\\$2b\\$%s\\$(%s|%s){1,70}",ROUND,LETTRE_CHIFFRE,SYMBOLE);
    private final String SHA3_HEX = String.format("%s{30,200}",LETTRE_CHIFFRE);
    private final String SALT_SIZE = String.format("%s{2}",CHIFFRE);
    private final String RANDOM22 = String.format("(%s|%s){22}",LETTRE_CHIFFRE,SYMBOLE);
    private final String SALT = RANDOM22;
    private final String MESSAGE = String.format("%s{1,200}",CARACTERE_IMPRIMABLE);
    private final String MESSAGE_INTERNE = String.format("%s{1,500}",CARACTERE_IMPRIMABLE);
    private final String NOM_UTILISATEUR = String.format("%s{5,20}",LETTRE_CHIFFRE);
    private final String TAG = String.format("#%s{5,20}",LETTRE_CHIFFRE);
    private final String NOM_DOMAINE = String.format("%s@%s",NOM_UTILISATEUR,DOMAINE);
    private final String TAG_DOMAINE = String.format("%s@%s",TAG,DOMAINE);
    private final String ID_DOMAINE = String.format("%s{1,5}@%s",CHIFFRE,DOMAINE);

    // Echanges entre le client et le Murmur Server :
    private final String HELLO = String.format("HELLO%s%s%s%s%s",ESP,DOMAINE,ESP,RANDOM22,CRLF);
    private final String CONNECT = String.format("CONNECT%s%s%s",ESP,NOM_UTILISATEUR,CRLF);
    private final String PARAM = String.format("PARAM%s%s%s%s%s",ESP,ROUND,ESP,SALT,CRLF);
    private final String CONFIRM = String.format("CONFIRM%s%s%s",ESP,SHA3_HEX,CRLF);
    private final String REGISTER = String.format("REGISTER%s%s%s%s%s%s%s",ESP,NOM_UTILISATEUR,ESP,SALT_SIZE,ESP,BCRYPT_HASH,CRLF);
    private final String FOLLOW = String.format("FOLLOW%s(%s|%s)%s",ESP,NOM_DOMAINE,TAG_DOMAINE,CRLF);
    private final String MSG = String.format("MSG%s%s%s",ESP,MESSAGE,CRLF);
    private final String MSGS = String.format("MSGS%s%s%s%s%s",ESP,NOM_DOMAINE,ESP,MESSAGE,CRLF);
    private final String OK = String.format("\\+OK%s%s%s",ESP,MESSAGE,CRLF);
    private final String ERREUR = String.format("\\-ERR%s%s%s",ESP,MESSAGE,CRLF);
    private final String DISCONNECT = String.format("DISCONNECT%s",CRLF);

    // Echanges multicast entre Murmur Server et Murmur Relay :
    private final String ECHO = String.format("ECHO%s%s%s%s%s",ESP,PORT,ESP,DOMAINE,CRLF);

    // Echanges unicast entre Murmur Server et Murmur Relay :
    private final String SEND = String.format("SEND%s%s%s%s%s(%s|%s)%s%s%s",ESP,ID_DOMAINE,ESP,NOM_DOMAINE,ESP,NOM_DOMAINE,TAG_DOMAINE,ESP,MESSAGE_INTERNE,CRLF);

/*    public static void main(String[] args)
    {
        Protocol p = new Protocol();
        System.out.println("$2b$14$Kc6uJG2Ty8yj5.jJFqte4.C.IG6OsHea3MFZ9pRwB7J71n2FbtS0O".matches(p.BCRYPT_HASH));
    }*/
}