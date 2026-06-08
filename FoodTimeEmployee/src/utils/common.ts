// 通用工具
export const checkProcessEnv = () => {
  return import.meta.env.VITE_APP_DELETE_PERMISSIONS === 'true'
}

export const debounce = (fn: Function, time?: number) => {
  time = time || 200
  let timer: any = null
  return function (this: any, ...args: any[]) {
    const _this = this
    if (timer) {
      clearTimeout(timer)
    }
    timer = setTimeout(function () {
      timer = null
      fn.apply(_this, args)
    }, time)
  }
}

// 节流
export const throttle = (fn: Function, time?: number) => {
  let timer: any = null
  time = time || 1000
  return function (this: any, ...args: any[]) {
    if (timer) {
      return
    }
    const _this = this
    timer = setTimeout(() => {
      timer = null
    }, time)
    fn.apply(_this, args)
  }
}

// 判断正、负
export const strIncrease = (str: string) => {
  if (str.slice(0, 1) === '-') {
    return true
  }
}
