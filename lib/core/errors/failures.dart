abstract class Failure {
  final String message;
  const Failure(this.message);

  @override
  String toString() => message;
}

class DatabaseFailure extends Failure {
  const DatabaseFailure(super.message);
}

class GoogleAuthFailure extends Failure {
  const GoogleAuthFailure(super.message);
}

class GoogleDriveFailure extends Failure {
  const GoogleDriveFailure(super.message);
}

class GoogleCalendarFailure extends Failure {
  const GoogleCalendarFailure(super.message);
}

class ReminderFailure extends Failure {
  const ReminderFailure(super.message);
}
