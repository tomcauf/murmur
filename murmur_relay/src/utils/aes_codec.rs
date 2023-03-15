use aes::cipher::generic_array::GenericArray;
use aes_gcm::{Aes256Gcm, aead::Aead, KeyInit, Nonce};


pub struct AESCodec;

impl AESCodec {
    pub fn encrypt(key : &str, message: &str) -> Result<Vec<u8>, aes_gcm::Error>{
        let key = GenericArray::from_slice(key.as_bytes());
        let cipher = Aes256Gcm::new(&key);
        let nonce = Nonce::from_slice(b"unique nonce");
        let ciphertext = cipher.encrypt(nonce, message.as_ref())?;
        Ok(ciphertext)
    }
    
    pub fn decrypt(key : &str, message: Vec<u8>) -> Result<String, aes_gcm::Error>{
        //let key = Aes256Gcm::generate_key(&mut OsRng);
        //Utilise la key en paramètre :
        let key = GenericArray::from_slice(key.as_bytes());
        let cipher = Aes256Gcm::new(&key);
        let nonce = Nonce::from_slice(b"unique nonce");
        let plaintext = cipher.decrypt(nonce, message.as_ref())?;
        let text_string = String::from_utf8(plaintext).unwrap();
        Ok(text_string)
    }
}