use crate::domains::relay::Relay;
use crate::domains::server::Server;
use crate::grammar::protocol::Protocol;
use crate::servers::server_runnable::{ServerRunnable, self};
use crate::{NetChooser, infrastructures::json_repositories::JsonRepositories};
use std::collections::HashMap;
use std::net::{UdpSocket, SocketAddr, Ipv4Addr};
use std::rc::Rc;
use std::sync::{Mutex, Arc};
use std::thread::Thread;

#[derive(Clone)]
pub struct RelayManager{
    repositories : JsonRepositories,
    net_chooser : NetChooser,
    protocol : Protocol,
    server_list : Arc<Mutex<HashMap<String, ServerRunnable>>>
}
impl RelayManager {
    
    pub fn new(repositories : JsonRepositories, net_chooser : NetChooser, protocol : Protocol) -> Self {
        RelayManager {
            repositories,
            net_chooser,
            protocol,
            server_list : Arc::new(Mutex::new(HashMap::new())),
        }
    }

    pub fn start(&self) {
        let relay = self.repositories.get_relay();
        let selected_interface = self.net_chooser.select_interface();
        //Affiche les info de selection de l'interface
        println!("Selected interface: {}", selected_interface.name);
        //Get ipv4 address
        let ipv4_addr = selected_interface.addr.iter().find(|addr| addr.ip().is_ipv4());
        if ipv4_addr.is_none() {
            println!("No ipv4 address found for selected interface");
            return;
        }else{
            println!("Selected interface ipv4 address: {}", ipv4_addr.unwrap().ip());
            let ipv4adrr : Ipv4Addr = ipv4_addr.unwrap().ip().to_string().parse().unwrap();
            std::thread::sleep(std::time::Duration::from_secs(1));
            match Self::multicast_function(self,relay, ipv4adrr) {
                Ok(_) => print!("Ok"),
                Err(err) => println!("Error: {}", err)
            }
        }
    }

    fn multicast_function(&self,relay: Relay, ipv4 : Ipv4Addr) -> Result<(), std::io::Error> {
        //You need to set reuseAddr: true when creating your socket:
        let multicast_socket = UdpSocket::bind(format!("0.0.0.0:{}", relay.get_multicast_port()))?;

        // Définit l'option SO_REUSEADDR pour le socket
        //Ne fonctionne pas
        /*let fd = multicast_socket.as_raw_fd();
        let optval: c_int = 1;
        let result = unsafe {
            setsockopt(
                fd,
                SOL_SOCKET,
                SO_REUSEPORT,
                &optval as *const c_int as *const libc::c_void,
                std::mem::size_of::<c_int>() as socklen_t,
            )
        };*/

        let multicast_addr: std::net::SocketAddrV4 = format!("{}:{}", relay.get_multicast_address(), 0).parse().unwrap();

        multicast_socket.join_multicast_v4(&multicast_addr.ip(), &ipv4)?;
        multicast_socket.set_nonblocking(true)?;
        println!("Listening for multicast messages on {}", multicast_addr);
        println!("Multicast messages will be sent to {}", relay.get_multicast_address());
        let mut buf = [0; 1024];
        loop {
            match multicast_socket.recv_from(&mut buf) {
                Ok((amt, src)) => {
                    let message_received = String::from_utf8_lossy(&buf[..amt]);
                    println!("Received {} bytes from {}", amt, src);
                    println!("Message received: {}", message_received);
                    RelayManager::handle_message(self,&message_received, src);
                }
                Err(ref e) if e.kind() == std::io::ErrorKind::WouldBlock => {
                    //println!("Waiting for multicast message");
                }
                Err(e) => {
                    println!("Error receiving multicast message: {}", e);
                }
            }
            drop(&multicast_socket);
        }
    }

    fn handle_message(&self, message_received: &str, ip_address: SocketAddr) {
        let check_message = self.protocol.verify_message(message_received);
        if check_message[0] == "ECHO" {
            println!("Message received is an echo message : {}", check_message[0]);
            let port = &check_message[1];
            let domain = &check_message[2];
            let relay = self.repositories.get_relay();
            let server = relay.get_server(domain.as_str());
            let socket = SocketAddr::new(ip_address.ip(), port.parse::<u16>().unwrap());
            if (server.get_domain() != "null" && server.get_base64_aes() != "null") && (self.server_list.lock().unwrap().contains_key(domain) == false) {
               let server_runnable = ServerRunnable::new(self.clone(),server.get_domain().to_string(), server.get_base64_aes().to_string(), self.protocol.clone(),socket);

                let mut server_list = self.server_list.lock().unwrap();
                server_list.insert(domain.to_string(), server_runnable);

                let server_runnable = server_list.get(domain).unwrap();
                let runnable = server_runnable.clone();
                std::thread::spawn(move || {
                    runnable.start();
                });
            }
        }else{
            println!("Message received is not an echo message : {}", message_received);
        }
    }
    
    pub fn remove_server(&self, domain: String) {
        let mut server_list = self.server_list.lock().unwrap();
        server_list.remove(&domain);
    }
    pub fn send_message(&self, domain: String, message: String){
        let server_list = self.server_list.lock().unwrap();
        server_list.get(&domain).unwrap().send_message(message);
    }
}