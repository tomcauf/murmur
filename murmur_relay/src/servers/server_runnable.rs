use std::{io::{Read, Write}, net::{SocketAddr, TcpStream}, rc::Rc, sync::{Arc, Mutex}, collections::HashMap};

use crate::{relay::relay_manager::RelayManager, domains::server, grammar::protocol::Protocol, utils::aes_codec::AESCodec};

use aes::{Aes128, Block};
use aes::cipher::{
    BlockCipher, BlockEncrypt, BlockDecrypt, KeyInit,
    generic_array::GenericArray,
};


#[derive(Clone)]
pub struct ServerRunnable{
    relay_manager : RelayManager,
    domain: String,
    base64_aes: String,
    protocol : Protocol,
    socket_addr : SocketAddr,
    message : Arc<Mutex<Vec<String>>>,
}

impl ServerRunnable{
    pub fn new(relay_manager : RelayManager,domain: String, base64_aes: String,protocol : Protocol, socket_addr: SocketAddr) -> ServerRunnable {
        ServerRunnable {
            relay_manager,
            domain,
            base64_aes,
            protocol,
            socket_addr,
            message : Arc::new(Mutex::new(Vec::new())),
        }
    }
    pub fn start(&self){
        let mut buffer = [0; 1024];
        println!("[[*]] ServerRunnable run : {}", self.socket_addr);
        let mut stream = TcpStream::connect(self.socket_addr).unwrap();
        stream = stream.try_clone().unwrap();
        stream.set_nonblocking(true).unwrap();
        loop {
            match stream.read(&mut buffer) {
                Ok(size) => {
                    let message_received = String::from_utf8_lossy(&buffer[..size]);
                    if size > 0 {
                        println!("Message received: {}", message_received);
                        self.handle_message(&message_received);
                    }
                }
                Err(ref e) if e.kind() == std::io::ErrorKind::WouldBlock => {
                    //println!("Waiting for multicast message: {}", &self.base64_aes);
                }
                Err(e) => {
                    println!("Error receiving multicast message: {}", e);
                    self.relay_manager.remove_server(self.domain.clone());
                }
            }
            
            let mut message = self.message.lock().unwrap();
            if message.len() > 0 {
                let message_to_send = message.remove(0);
                stream.write(message_to_send.as_bytes()).unwrap();
            }
        }
    }
    fn handle_message(&self, message_received: &str) {
        //let message = AESCodec::decrypt(&self.base64_aes, message_received.as_bytes().to_vec()).unwrap();
        //println!("Message received: {}",message);
        let check_message = self.protocol.verify_message(message_received);
        println!("Message received: {:?}",check_message);
        if check_message[0] == "SEND" {
            let id_domain = &check_message[1];
            let nom_domaine = &check_message[2];
            let nom_tag_domaine = &check_message[3];
            let message = &check_message[4];
            let mut nom_tag_domaine_split = nom_tag_domaine.split("@");
            let domaine_to_send = nom_tag_domaine_split.nth(1).unwrap();
            self.relay_manager.send_message(domaine_to_send.to_string(), message_received.to_string());
        }
    }
    pub fn add_message(&self, new_message : String){
        let mut message = self.message.lock().unwrap();
        message.push(new_message);
    }
}