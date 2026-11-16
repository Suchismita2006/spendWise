from datetime import date

from cli.transaction_menu import transaction_menu
from cli.budget_menu import budget_menu
from cli.goal_menu import goal_menu

from services.analytics_service import (
    monthly_summary,
    category_summary
)


def show_monthly_summary(user_id):

    print("\n========== MONTHLY SUMMARY ==========")

    try:
        month = int(
            input("Month (1-12): ")
        )

        year = int(
            input("Year: ")
        )

    except ValueError:
        print("Month and year must be numbers.")
        return

    if month < 1 or month > 12:
        print("Invalid month.")
        return

    result = monthly_summary(
        user_id,
        month,
        year
    )

    if result is None:
        return

    print("\n--------------------------------")
    print(f"Total Income  : ₹{result['total_income']}")
    print(f"Total Expenses: ₹{result['total_expenses']}")
    print(f"Balance       : ₹{result['balance']}")
    print("--------------------------------")


def show_category_summary(user_id):

    print("\n========== CATEGORY SUMMARY ==========")

    try:
        month = int(input("Month (1-12): "))
        year = int(input("Year: "))

    except ValueError:
        print("Invalid month or year.")
        return

    categories = category_summary(
        user_id,
        month,
        year
    )

    if not categories:
        print("No expense data found.")
        return

    print("\nCategory-wise Expenses")
    print("--------------------------------")

    for category in categories:

        print(
            f"{category['category']:<20}"
            f"₹{category['total_spent']}"
        )


def main_menu(user):

    user_id = user["user_id"]

    while True:

        print("\n================================")
        print("       SPENDWISE DASHBOARD")
        print("================================")

        print(f"Welcome, {user['name']}!")

        print("\n1. Transactions")
        print("2. Monthly Summary")
        print("3. Category Summary")
        print("4. Budgets")
        print("5. Savings Goals")
        print("6. Logout")

        choice = input(
            "\nEnter your choice: "
        ).strip()

        if choice == "1":
            transaction_menu(user_id)

        elif choice == "2":
            show_monthly_summary(user_id)

        elif choice == "3":
            show_category_summary(user_id)

        elif choice == "4":
            budget_menu(user_id)

        elif choice == "5":
            goal_menu(user_id)

        elif choice == "6":
            print("Logged out successfully.")
            break

        else:
            print("Invalid choice.")