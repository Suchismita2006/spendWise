from datetime import date

from services.transaction_service import (
    add_transaction,
    get_transactions,
    update_transaction,
    delete_transaction
)

from utils.validators import (
    validate_amount,
    validate_date
)


def add_transaction_menu(user_id, transaction_type):
    print(f"\n====== ADD {transaction_type.upper()} ======")

    category = input("Category: ").strip()

    amount = input("Amount: ").strip()

    if not validate_amount(amount):
        print("Invalid amount.")
        return

    description = input("Description: ").strip()

    transaction_date = input(
        "Date (YYYY-MM-DD) [leave blank for today]: "
    ).strip()

    if not transaction_date:
        transaction_date = date.today().isoformat()

    if not validate_date(transaction_date):
        print("Invalid date.")
        return

    add_transaction(
        user_id,
        transaction_type,
        category,
        float(amount),
        description,
        transaction_date
    )


def view_transactions_menu(user_id):
    transactions = get_transactions(user_id)

    if not transactions:
        print("\nNo transactions found.")
        return

    print("\n================ TRANSACTIONS ================")

    print(
        f"{'ID':<5}"
        f"{'Type':<10}"
        f"{'Category':<15}"
        f"{'Amount':<15}"
        f"{'Date':<15}"
        f"{'Description'}"
    )

    print("-" * 80)

    for transaction in transactions:

        print(
            f"{transaction['transaction_id']:<5}"
            f"{transaction['type']:<10}"
            f"{transaction['category']:<15}"
            f"₹{transaction['amount']:<14}"
            f"{str(transaction['transaction_date']):<15}"
            f"{transaction['description'] or '-'}"
        )


def update_transaction_menu(user_id):
    view_transactions_menu(user_id)

    try:
        transaction_id = int(
            input("\nEnter transaction ID to update: ")
        )
    except ValueError:
        print("Invalid transaction ID.")
        return

    transaction_type = input(
        "Type (income/expense): "
    ).strip().lower()

    if transaction_type not in ("income", "expense"):
        print("Invalid transaction type.")
        return

    category = input("Category: ").strip()

    amount = input("Amount: ").strip()

    if not validate_amount(amount):
        print("Invalid amount.")
        return

    description = input("Description: ").strip()

    transaction_date = input(
        "Date (YYYY-MM-DD): "
    ).strip()

    if not validate_date(transaction_date):
        print("Invalid date.")
        return

    update_transaction(
        user_id,
        transaction_id,
        transaction_type,
        category,
        float(amount),
        description,
        transaction_date
    )


def delete_transaction_menu(user_id):
    view_transactions_menu(user_id)

    try:
        transaction_id = int(
            input("\nEnter transaction ID to delete: ")
        )
    except ValueError:
        print("Invalid transaction ID.")
        return

    confirmation = input(
        "Are you sure? (y/n): "
    ).strip().lower()

    if confirmation == "y":
        delete_transaction(user_id, transaction_id)

    else:
        print("Delete cancelled.")


def transaction_menu(user_id):

    while True:

        print("\n========== TRANSACTIONS ==========")

        print("1. Add Income")
        print("2. Add Expense")
        print("3. View Transactions")
        print("4. Update Transaction")
        print("5. Delete Transaction")
        print("6. Back")

        choice = input("\nEnter your choice: ").strip()

        if choice == "1":
            add_transaction_menu(user_id, "income")

        elif choice == "2":
            add_transaction_menu(user_id, "expense")

        elif choice == "3":
            view_transactions_menu(user_id)

        elif choice == "4":
            update_transaction_menu(user_id)

        elif choice == "5":
            delete_transaction_menu(user_id)

        elif choice == "6":
            break

        else:
            print("Invalid choice.")