package tasksmanager;

class TaskException extends Exception {
  String shortMsg = null;
  String longMsg = null;

  TaskException(String _msg) {
    super();
    shortMsg = _msg;
    longMsg = _msg;
  }

  TaskException(String _shortMsg, String _longMsg) {
    super();
    shortMsg = _shortMsg;
    longMsg = _longMsg;
  }
}
