package sceptre

trait LogProvider {
  def trace(msg: => String)
  def debug(msg: => String)
  def info(msg: => String)
  def warn(msg: => String)
  def warn(msg: => String, cause: Throwable)
  def fatal(msg: => String)
  def fatal(msg: => String, cause: Throwable)
}
