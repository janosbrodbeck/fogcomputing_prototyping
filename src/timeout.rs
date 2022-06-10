use std::collections::HashMap;
use std::time::Duration;


// generic way to use for Event and EventResponse as message
struct TimeoutableRequest<T: ::prost::Message> {
    message: T,
    timeout: Timeout,
}

// probably obsolete and can be put into the TimeoutableRequest completely.
// especially since we need to be able to retrieve the message later as well
struct Timeout {
    timeout: Duration,
    current_retry: u8,
}

// probably can be just used as a u64 directly. would be better to be immutable tho...
// think this maybe wraps the u64 such that it cannot be accessed outside of this module?
struct TimeoutHandle {
    id: u64,
}

struct TimeoutManager {
    timeouts: HashMap<u64, Timeout>,
    min: Duration,
    max: Duration,
    max_retries: u8,
    handle_counter: u64,
}

impl TimeoutManager {
    const DEFAULT_MIN_DURATION: Duration = Duration::from_millis(100);
    const DEFAULT_MAX_DURATION: Duration = Duration::from_secs(600); // 10 min
    const DEFAULT_RETRIES: u8 = 5;

    pub fn new() -> Self {
        TimeoutManager {
            timeouts: HashMap::with_capacity(100),
            min: TimeoutManager::DEFAULT_MIN_DURATION,
            max: TimeoutManager::DEFAULT_MAX_DURATION,
            max_retries: TimeoutManager::DEFAULT_RETRIES,
            handle_counter: 0
        }
    }

    pub fn put(&mut self) -> TimeoutHandle {
        self.timeouts.insert(self.handle_counter, Timeout {
            timeout: Duration::from(self.min),
            current_retry: 0,
        }).unwrap();
        let ret = self.handle_counter;
        self.handle_counter += 1;
        TimeoutHandle {
            id: ret,
        }
    }

    pub fn get(&self, handle: &TimeoutHandle) -> Duration {
        self.timeouts.get(&handle.id).unwrap().timeout
    }

}


impl Timeout {
    const DEFAULT_TIMEOUT_MILLIS: u64 = 100;

    pub const fn new() -> Self {
        Timeout {
            timeout: Duration::from_millis(Timeout::DEFAULT_TIMEOUT_MILLIS),
            current_retry: 0,
        }
    }

    pub fn get(&self) -> Duration {
        self.timeout
    }

    pub fn increase(&mut self) {
        self.timeout = self.timeout.saturating_mul(2)
    }

    pub fn reset(&mut self) {
        self.timeout = Duration::from_millis(Timeout::DEFAULT_TIMEOUT_MILLIS)
    }

}
