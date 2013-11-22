__author__ = 'Mattias Hellborg Arthursson'

from time import sleep
from fabric.api import task, settings, sudo, execute
from boto.ec2 import EC2Connection, get_region, connect_to_region

BASE_AMI_ID = 'ami-6d0be61a'
KEYPAIR = 'dev-keypair'
SECURITY_GROUPS = ['default']
INSTANCE_TYPE = "m1.small"
KEY_FILE = '~/.ssh/dev-keypair.pem'
SSH_USER = 'ubuntu'
AMI_NAME = 'spring-ldap-openldap'
AMI_DESCRIPTION = 'OpenLdap instance for Spring LDAP integration tests'

@task
def launch_instance():
    conn = connect_to_region('eu-west-1')
    reservation = conn.run_instances(
        BASE_AMI_ID,
        key_name=KEYPAIR,
        instance_type=INSTANCE_TYPE,
        security_groups=SECURITY_GROUPS
    )

    instance = reservation.instances[0]
    while instance.state != 'running':
        print('Waiting for instance to start')
        sleep(10)
        instance.update()

    print('Instance started')
    return conn, instance

@task
def setup_openldap_instance():
    conn, instance = launch_instance()

    with settings(
            host_string=instance.public_dns_name,
            key_filename=KEY_FILE,
            user=SSH_USER,
            connection_attempts=10):
        upgrade_instance()
        install_slapd()

    return conn, instance

@task
def bundle_openldap_instance():
    conn, instance = setup_openldap_instance()
    bundle_instance(conn, instance.id, AMI_NAME, AMI_DESCRIPTION)


def bundle_instance(conn, instance_id, ami_name, ami_description):
    print('Creating AMI')
    new_id = conn.create_image(instance_id, ami_name, ami_description)
    ami = conn.get_all_images([new_id])[0]

    while ami.state == 'pending':
        print('Waiting for image to become available to start')
        ami.update()
        sleep(20)

    conn.create_tags([ami.id], {'Name': ami_name})
    conn.terminate_instances([instance_id])


def upgrade_instance():
    sudo('apt-get update')
    sudo('apt-get upgrade -y')
    sudo('apt-get install debconf-utils -y')


def install_slapd():
    sudo('cp /etc/hosts /etc/hosts.old')
    sudo("sed -i '1i 127.0.1.1 open-ldap.261consulting.com open-ldap' /etc/hosts")
    sudo("echo 'slapd slapd/password1 password secret' | sudo debconf-set-selections")
    sudo("echo 'slapd slapd/password2 password secret' | sudo debconf-set-selections")
    sudo("echo 'slapd shared/organization string 261consulting.com' | sudo debconf-set-selections")
    sudo("echo 'slapd slapd/organization string 261consulting.com' | sudo debconf-set-selections")
    sudo("echo 'slapd slapd/domain string 261consulting.com' | sudo debconf-set-selections")
    sudo('apt-get install slapd ldap-utils -y')