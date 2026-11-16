from getpass import getpass

from services.auth_service import register_user, login_user
from utils.validators import (
    validate_name,
    validate_email,
    validate_password
)


def handle_registration():
    print("\n========== REGISTER ==========")

    name = input("Name: ").strip()
    email = input("Email: ").strip().lower()
    password = getpass("Password: ")

    if not validate_name(name):
        print("Invalid name.")
        return

    if not validate_email(email):
        print("Invalid email address.")
        return

    if not validate_password(password):
        print("Password must contain at least 6 characters.")
        return

    register_user(name, email, password)


def handle_login():
    print("\n========== LOGIN ==========")

    email = input("Email: ").strip().lower()
    password = getpass("Password: ")

    if not validate_email(email):
        print("Invalid email address.")
        return None

    user = login_user(email, password)

    return user


def auth_menu():
    while True:

        print("\n==============================")
        print("          SPENDWISE")
        print("==============================")

        print("1. Register")
        print("2. Login")
        print("3. Exit")

        choice = input("\nEnter your choice: ").strip()

        if choice == "1":
            handle_registration()

        elif choice == "2":
            user = handle_login()

            if user:
                return user

        elif choice == "3":
            return None

        else:
            print("Invalid choice.")