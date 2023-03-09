use utils::net_chooser::NetChooser;
use if_addrs::get_if_addrs;

mod utils;
fn main() {
    //let net_chooser = NetChooser::new();
    let mut net_chooser = NetChooser::new();
    net_chooser.select_interface();
    let selected_interface = net_chooser.get_interface_information();
    match selected_interface {
        Some(interface) => {
            println!("Selected interface: {}", interface.name);
            match interface.mac_addr {
                Some(mac) => println!("   MAC Address: {}", mac),
                None => println!("   MAC Address: unknown"),
            }
            println!("   IP Addresses: ");
            if let Ok(if_addrs) = get_if_addrs() {
                for if_addr in if_addrs.iter() {
                    if if_addr.name == interface.name && if_addr.addr.ip().is_ipv4() {
                        println!("      {}", if_addr.addr.ip());
                    }
                }
            }
        },
        None => println!("Invalid selection."),
    }
}