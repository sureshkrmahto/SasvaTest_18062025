export function useLocalStorage<T>(key: string, initialValue: T) {
  const readValue = (): T => {
    if (typeof window === 'undefined') return initialValue;
    try {
      const item = window.localStorage.getItem(key);
      return item ? (JSON.parse(item) as T) : initialValue;
    } catch {
      return initialValue;
    }
  };

  let currentValue = readValue();

  const setValue = (value: T | ((val: T) => T)) => {
    const newValue = value instanceof Function ? value(currentValue) : value;
    currentValue = newValue;
    try {
      window.localStorage.setItem(key, JSON.stringify(newValue));
    } catch {
      // ignore
    }
  };

  const getValue = () => currentValue;

  return { getValue, setValue };
}
