from services.budget_service import (
    set_budget,
    get_budgets,
    delete_budget
)

from utils.validators import (
    validate_amount,
    validate_month,
    validate_year
)


def set_budget_menu(user_id):
    print("\n========== SET BUDGET ==========")

    category = input("Category: ").strip()

    amount = input("Budget amount: ").strip()

    if not validate_amount(amount):
        print("Invalid amount.")
        return

    month = input("Month (1-12): ").strip()

    if not validate_month(month):
        print("Invalid month.")
        return

    year = input("Year: ").strip()

    if not validate_year(year):
        print("Invalid year.")
        return

    set_budget(
        user_id,
        category,
        float(amount),
        int(month),
        int(year)
    )


def view_budgets_menu(user_id):

    month = input("Month (1-12): ").strip()

    if not validate_month(month):
        print("Invalid month.")
        return

    year = input("Year: ").strip()

    if not validate_year(year):
        print("Invalid year.")
        return

    budgets = get_budgets(
        user_id,
        int(month),
        int(year)
    )

    if not budgets:
        print("No budgets found.")
        return

    print("\n========== BUDGETS ==========")

    for budget in budgets:

        print(
            f"ID: {budget['budget_id']} | "
            f"{budget['category']} | "
            f"₹{budget['amount']}"
        )


def delete_budget_menu(user_id):

    try:
        budget_id = int(
            input("Budget ID: ")
        )

    except ValueError:
        print("Invalid budget ID.")
        return

    confirmation = input(
        "Delete this budget? (y/n): "
    ).strip().lower()

    if confirmation == "y":
        delete_budget(user_id, budget_id)


def budget_menu(user_id):

    while True:

        print("\n========== BUDGET ==========")

        print("1. Set Budget")
        print("2. View Budgets")
        print("3. Delete Budget")
        print("4. Back")

        choice = input("\nEnter your choice: ").strip()

        if choice == "1":
            set_budget_menu(user_id)

        elif choice == "2":
            view_budgets_menu(user_id)

        elif choice == "3":
            delete_budget_menu(user_id)

        elif choice == "4":
            break

        else:
            print("Invalid choice.")