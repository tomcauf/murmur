use std::{path::Path, result};
use grammar::protocol::Protocol;
use infrastructures::json_repositories::JsonRepositories;
use relay::{relay_manager::RelayManager};
use utils::{net_chooser::NetChooser, aes_codec::AESCodec};

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

    /*let message = "k9Ut583O2/8os1u+MeLfBknJaxzvhAyU4YHcYR9eNUZMWNxUXPA7+QEaPSigz/shxOo7KeoNxobWFje9yasRQlY8YkY/qgXMeGV2MhQrxA2UqCnLJu26EMh/vsI2wqMufawSQFK8r0xeyH6QYVSeENwfV6QgnQ/QHYFqSuNxLwYJNbLTMQsLHxhp1eS7n/X4/w==qymu4sdMvjUIYb5T";

    let aes_codec = AESCodec::new();

    let result = aes_codec.decrypt("OpoyGQKQL66hSkxw50UMWeR3BcN2M+2PgKGvszDVuCE=",message.as_bytes().to_vec());

    let result = aes_codec.encrypt("OpoyGQKQL66hSkxw50UMWeR3BcN2M+2PgKGvszDVuCE=", "SEND 1@server2.godswila.guru swilabus@server2.godswila.guru lswinnen@server1.godswila.guru FOLLOW lswinnen@server1.godswila.guru");

    println!("Result : {:?}", result);*/
}