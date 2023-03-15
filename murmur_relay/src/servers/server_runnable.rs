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
}

impl ServerRunnable{
    pub fn new(relay_manager : RelayManager,domain: String, base64_aes: String,protocol : Protocol, socket_addr: SocketAddr) -> ServerRunnable {
        ServerRunnable {
            relay_manager,
            domain,
            base64_aes,
            protocol,
            socket_addr,
        }
    }
    pub fn start(&self){
        let mut buffer = [0; 1024];
        print!("ServerRunnable run : {}", self.socket_addr);
        //Se connecter avec l'ip 192.168.0.5 et le port 12021
        let mut stream = TcpStream::connect(self.socket_addr).unwrap();
        stream = stream.try_clone().unwrap();
        stream.set_nonblocking(true).unwrap();
        loop {
            match stream.read(&mut buffer) {
                Ok(size) => {
                    let message_received = String::from_utf8_lossy(&buffer[..size]);
                    if size > 0 {
                        self.handle_message(&message_received);
                    }
                }
                Err(ref e) if e.kind() == std::io::ErrorKind::WouldBlock => {
                    //println!("Waiting for multicast message");
                }
                Err(e) => {
                    println!("Error receiving multicast message: {}", e);
                    self.relay_manager.remove_server(self.domain.clone());
                }
            }
            drop(&stream);
        }
    }
    fn handle_message(&self, message_received: &str) {
        let message = AESCodec::decrypt(&self.base64_aes, message_received.as_bytes().to_vec()).unwrap();
        println!("Message received: {}",message);
        let check_message = self.protocol.verify_message(message_received);
        if check_message[0] == "SEND" {
            let id_domain = &check_message[1];
            let nom_domaine = &check_message[2];
            let nom_tag_domaine = &check_message[3];
            let message = &check_message[4];
            let mut nom_tag_domaine_split = nom_tag_domaine.split("@");
            let domaine_to_send = nom_tag_domaine_split.nth(1).unwrap();
            //TODO: Voir ce que je dois envoyer exactement
            self.relay_manager.send_message(domaine_to_send.to_string(), message.to_string());
        }
    }
    pub fn send_message(&self, message : String){
        let mut stream = TcpStream::connect(self.socket_addr).unwrap();
        let message_aes = AESCodec::encrypt(&self.base64_aes, &message).unwrap();
        stream.write(&message_aes).unwrap();
    }
}