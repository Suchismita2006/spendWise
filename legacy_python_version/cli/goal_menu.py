from services.goal_service import (
    create_goal,
    get_goals,
    update_saved_amount,
    delete_goal
)

from utils.validators import (
    validate_amount,
    validate_date
)


def create_goal_menu(user_id):

    print("\n========== CREATE SAVINGS GOAL ==========")

    goal_name = input("Goal name: ").strip()

    target_amount = input("Target amount: ").strip()

    if not validate_amount(target_amount):
        print("Invalid target amount.")
        return

    target_date = input(
        "Target date (YYYY-MM-DD, optional): "
    ).strip()

    if target_date and not validate_date(target_date):
        print("Invalid date.")
        return

    if not target_date:
        target_date = None

    create_goal(
        user_id,
        goal_name,
        float(target_amount),
        target_date
    )


def view_goals_menu(user_id):

    goals = get_goals(user_id)

    if not goals:
        print("No savings goals found.")
        return

    print("\n========== SAVINGS GOALS ==========")

    for goal in goals:

        remaining = (
            goal["target_amount"]
            - goal["saved_amount"]
        )

        print(
            f"\nID: {goal['goal_id']}"
            f"\nGoal: {goal['goal_name']}"
            f"\nTarget: ₹{goal['target_amount']}"
            f"\nSaved: ₹{goal['saved_amount']}"
            f"\nRemaining: ₹{remaining}"
            f"\nTarget Date: {goal['target_date'] or 'Not set'}"
        )


def update_goal_menu(user_id):

    view_goals_menu(user_id)

    try:
        goal_id = int(
            input("\nGoal ID: ")
        )

    except ValueError:
        print("Invalid goal ID.")
        return

    saved_amount = input(
        "New total saved amount: "
    ).strip()

    if not validate_amount(saved_amount):
        print("Invalid amount.")
        return

    update_saved_amount(
        user_id,
        goal_id,
        float(saved_amount)
    )


def delete_goal_menu(user_id):

    try:
        goal_id = int(
            input("Goal ID: ")
        )

    except ValueError:
        print("Invalid goal ID.")
        return

    confirmation = input(
        "Delete this goal? (y/n): "
    ).strip().lower()

    if confirmation == "y":
        delete_goal(user_id, goal_id)


def goal_menu(user_id):

    while True:

        print("\n========== SAVINGS GOALS ==========")

        print("1. Create Goal")
        print("2. View Goals")
        print("3. Update Saved Amount")
        print("4. Delete Goal")
        print("5. Back")

        choice = input("\nEnter your choice: ").strip()

        if choice == "1":
            create_goal_menu(user_id)

        elif choice == "2":
            view_goals_menu(user_id)

        elif choice == "3":
            update_goal_menu(user_id)

        elif choice == "4":
            delete_goal_menu(user_id)

        elif choice == "5":
            break

        else:
            print("Invalid choice.")