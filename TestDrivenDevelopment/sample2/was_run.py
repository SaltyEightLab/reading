class WasRun:
    def __init__(self, name):
        self.wasRun = None
    def run(self):
        self.testMethod()
    def testMethod(self):
        self.wasRun = 1