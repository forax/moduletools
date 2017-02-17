package fr.umlv.moduletools.main;

class InvalidCommandLineException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public InvalidCommandLineException(String message) {
    super(message);
  }
  
  public InvalidCommandLineException(String message, Throwable cause) {
    super(message, cause);
  }
}
