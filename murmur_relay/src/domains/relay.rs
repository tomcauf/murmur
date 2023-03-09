use crate::domains::server::Server; // Importer le module server

pub struct Relay {
    multicast_address: String,
    multicast_port: usize,
    server_list: Vec<Server>,
}

impl Relay {
    pub fn new(multicast_address: String, multicast_port: usize, server_list: Vec<Server>) -> Relay {
        Relay {
            multicast_address,
            multicast_port,
            server_list: server_list
        }
    }

    pub fn get_multicast_address(&self) -> &str {
        &self.multicast_address
    }

    pub fn get_multicast_port(&self) -> usize {
        self.multicast_port
    }

    pub fn get_server_list(&self) -> &Vec<Server> {
        &self.server_list
    }
}