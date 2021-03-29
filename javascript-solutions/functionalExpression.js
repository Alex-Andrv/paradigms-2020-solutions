const nAryOperation = act => (firstExp, secondExp, ...argsExp) => (...args) => {
    let ans = act(firstExp(...args), secondExp(...args));
    for (let exp of argsExp) {
        ans = act(ans, exp(...args));
    }
    return ans;
}

const unaryOperation = act => exp => (...args) => act(exp(...args));

const cnst = (cnstValue) => (...args) => cnstValue;

const pi = cnst(Math.PI);

const e = cnst(Math.E);

const variable = (stVariable) => (...args) => {
    switch (stVariable) {
        case "x":
            return args[0];
        case "y":
            return args[1];
        case "z":
            return args[2];
    }
}

const add  = nAryOperation((first, second) => first + second);

const subtract = nAryOperation((first, second) => first - second);

const multiply = nAryOperation((first, second) => first * second);

const divide = nAryOperation((first, second) => first / second);

const negate = unaryOperation(value => - value);

const cos = unaryOperation(value => Math.cos(value));

const sin = unaryOperation(value => Math.sin(value));

function parse(expression) {
    const tokens = expression.split(' ');
    let stack = [];
    for (let token of tokens) {
        switch (token) {
            case "x":
            case "y":
            case "z":
                stack.push(variable(token));
                break;
            case "+":
                const firstAddExp = stack.pop();
                stack.push(add(stack.pop(), firstAddExp));
                break;
            case "-":
                const firstSubtractExp = stack.pop();
                stack.push(subtract(stack.pop(), firstSubtractExp));
                break;
            case "*":
                const firstMultiplyExp = stack.pop();
                stack.push(multiply(stack.pop(), firstMultiplyExp));
                break;
            case "/":
                const firstDivideExp = stack.pop();
                stack.push(divide(stack.pop(), firstDivideExp));
                break;
            case "sin":
                const firstSinExp = stack.pop();
                stack.push(sin(firstSinExp));
                break;
            case "cos":
                const firstCosExp = stack.pop();
                stack.push(cos(firstCosExp));
                break;
            case "negate":
                const firstNegateExp = stack.pop();
                stack.push(negate(firstNegateExp));
                break;
            case "pi":
                stack.push(pi);
                break;
            case "e":
                stack.push(e);
                break;
            case "":
                break;
            default:
                if (!isNaN(token)) {
                    stack.push(cnst(parseInt(token)));
                }
        }
    }
    return stack.pop();
}

