import ch.qos.logback.core.joran.spi.ConsoleTarget

def logDir = new File(System.getProperty("user.home"), "log").toString()
def logName = "repeater-cor.log"
def logTag = "%d{yyyyMMdd}"

appender("STDOUT", ConsoleAppender) {
    target = ConsoleTarget.SystemOut
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{5} - %msg%n"
    }
}

appender("FILE", RollingFileAppender) {
    append = true
    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = new File(logDir, String.format("%s-%s", logTag, logName)).toString()
        maxHistory = 30
    }
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{5} - %msg%n"
    }
}

root(WARN, ["STDOUT"])

logger("org.thehellnet.ham.repeater.cor", DEBUG)
