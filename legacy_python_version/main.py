from cli.auth_menu import auth_menu
from cli.main_menu import main_menu


def main():

    while True:

        user = auth_menu()

        if user is None:
            print("\nThank you for using SpendWise!")
            break

        main_menu(user)


if __name__ == "__main__":
    main()