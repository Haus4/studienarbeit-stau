from threading import Thread

class ReturnThread(Thread):
    def __init__(self, group=None, target=None, name=None,
                 args=(), kwargs={}, Verbose=None):
        Thread.__init__(self, group, target, name, args, kwargs, Verbose)
        self._return = None
    def run(self):
        if self._Thread__target is not None:
            self._return = self._Thread__target(*self._Thread__args,
                                                **self._Thread__kwargs)
    def join(self):
        Thread.join(self)
        return self._return

def start(group=None, target=None, name=None, args=(), kwargs={}, Verbose=None):
    thread = ReturnThread(group, target, name, args, kwargs, Verbose)
    thread.start()
    return thread
