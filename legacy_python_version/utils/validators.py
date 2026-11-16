import re
from datetime import datetime


def validate_name(name):
    return len(name.strip()) >= 2


def validate_email(email):
    pattern = r"^[\w\.-]+@[\w\.-]+\.\w+$"
    return re.match(pattern, email) is not None


def validate_password(password):
    return len(password) >= 6


def validate_amount(amount):
    try:
        amount = float(amount)
        return amount > 0
    except ValueError:
        return False


def validate_date(date_string):
    try:
        datetime.strptime(date_string, "%Y-%m-%d")
        return True
    except ValueError:
        return False


def validate_month(month):
    try:
        month = int(month)
        return 1 <= month <= 12
    except ValueError:
        return False


def validate_year(year):
    try:
        year = int(year)
        return 2000 <= year <= 2100
    except ValueError:
        return False