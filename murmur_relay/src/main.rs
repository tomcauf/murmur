use std::path::Path;
use grammar::protocol::Protocol;
use infrastructures::json_repositories::JsonRepositories;
use relay::{relay_manager::RelayManager};
use utils::net_chooser::NetChooser;

mod domains;
mod grammar;
mod infrastructures;
mod relay;
mod utils;
mod servers;

fn main() {
   let i_relay_repositories = JsonRepositories::new(
        Path::new("src/resources/relay.json")
            .to_str()
            .unwrap()
            .to_string(),
    );
    let relay_manager = RelayManager::new(
        i_relay_repositories,
        NetChooser::new(),
        Protocol::new(),
    );
    relay_manager.start();
    /*let protocol = Protocol::new();
    let messageProtocol = protocol.build_send("1@server1.godswila.guru","maxime123@server1.godswila.guru","maxime345@server2.godswila.guru","COUCOU JE SUIS LE MESSAGE du serveur 1");

    let messageTab = protocol.verify_message(&messageProtocol);
    println!("Message : {}",messageTab[0]); */
}