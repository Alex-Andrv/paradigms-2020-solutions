"use strict"

function BinOperation(firstExp, ...otherExp) {
    this.firstExp = firstExp;
    this.otherExp = otherExp;
    this.evaluate = (...args) => {
        let ans = this.firstExp.evaluate(...args);
        ans = this.otherExp.reduce((sum, current) => this.operation(sum, current.evaluate(...args)), ans);
        return ans;
    };
    this.toString = () => {
        let ans = this.firstExp.toString();
        ans = otherExp.reduce((acc, current) => acc + " " + current.toString() + " " + this.getOperator(), ans);
        return ans;
    };
    this.getSecondArg = (args) => this.otherExp.length > 1 ?
        this.createThisFunction(...this.otherExp) :
        this.otherExp[0].createCopyThisFunction();
    this.diff = (diffVariable) => {
        return this.myDiff(diffVariable, this.firstExp.createCopyThisFunction(), this.getSecondArg());
    };
    this.getExp = (nameFun) => this.firstExp[nameFun]() + this.otherExp.reduce(function (previousValue, currentItem) {
        return previousValue + " " + currentItem[nameFun]();
    }, "");
    this.prefix = () => "(" + this.getOperator() + " " + this.getExp("prefix") + ")";
    this.postfix = () => "(" + this.getExp("postfix") + " " + this.getOperator() + ")";
}

function UnaryOperation(exp) {
    this.exp = exp;
    this.evaluate = (...args) => {
        return this.operation(this.exp.evaluate(...args));
    };
    this.toString = () => {
        return this.exp.toString() + " " + this.getOperator();
    };
    this.prefix = () => "(" + this.getOperator() + " " + this.exp.prefix() + ")";
    this.postfix = () => "(" + this.exp.postfix() + " " + this.getOperator() + ")";
}

function Const(cnstValue) {
    this.value = cnstValue;
    this.createThisFunction = () => new Const(this.value);
    this.createCopyThisFunction = () => new Const(cnstValue);
    this.evaluate = (...args) => this.value;
    this.toString = () => this.value.toString();
    this.diff = (diffVariable) => new Const(0);
    this.prefix = () => this.value.toString();
    this.postfix = this.prefix;
}

function Variable(strVariable) {
    this.variable = strVariable;
    this.createThisFunction = () => new Variable(this.variable);
    this.createCopyThisFunction = () => new Variable(strVariable);
    this.diff = (diffVariable) => {
        if (this.variable === diffVariable) {
            return new Const(1);
        } else {
            return new Const(0);
        }
    };
    this.evaluate = (...args) => {
        switch (this.variable) {
            case "x":
                return args[0];
            case "y":
                return args[1];
            case "z":
                return args[2];
        }
    };
    this.toString = () => this.variable;
    this.prefix = () => this.variable;
    this.postfix = this.prefix;
}

function Multiply(...argsExp) {
    BinOperation.call(this, ...argsExp);
    this.createThisFunction = (...args) => new Multiply(...args);
    this.createCopyThisFunction = () => new Multiply(...argsExp);
    this.operation = (first, second) => first * second;
    this.getOperator = () => "*";
    this.myDiff = (diffVariable, firstExp, secondExp) => {
        return new Add(
            new Multiply(firstExp.diff(diffVariable), secondExp),
            new Multiply(firstExp, secondExp.diff(diffVariable)));
    };
}

function Add(...argsExp) {
    BinOperation.call(this, ...argsExp);
    this.createThisFunction = (...args) => new Add(...args);
    this.createCopyThisFunction = () => new Add(...argsExp);
    this.operation = (first, second) => first + second;
    this.getOperator = () => "+";
    this.myDiff = (diffVariable, firstExp, secondExp) => {
        return new Add(firstExp.diff(diffVariable), secondExp.diff(diffVariable));
    };
}

function nAryOperation(...argsExp) {
    this.argsExp = argsExp;
    this.evaluate = (...args) => this.argsExp.map(
        x => x.evaluate(...args)).reduce(this.operation, 0);
    this.toString = () => "(" + [...this.argsExp.map(x => x.toString())].join(" ") + " " + this.getOperator() + ")";
    this.embrace = (expression) => ["(", expression, ")"].join("");
    this.prefix = () => this.embrace([this.getOperator(), ...this.argsExp.map(x => x.prefix())].join(" "));
    this.postfix = () => this.embrace([...this.argsExp.map(x => x.postfix())].join(" ") + " " + this.getOperator());
    this.diff = (diffVariable) => {
        return this.createThisFunction(...this.argsExp.map(x => x.diff(diffVariable)));
    };
}

function Sum(...argsExp) {
    nAryOperation.call(this, ...argsExp);
    this.createThisFunction = (...args) => new Sum(...args);
    this.createCopyThisFunction = () => new Sum(...argsExp);
    this.operation = (accumulator, currentValue) => accumulator + currentValue;
    this.getOperator = () => "sum";
}

function Avg(...argsExp) {
    nAryOperation.call(this, ...argsExp);
    this.createThisFunction = (...args) => new Avg(...args);
    this.createCopyThisFunction = () => new Avg(...argsExp);
    this.operation = (accumulator, currentValue) => accumulator + currentValue / argsExp.length;
    this.getOperator = () => "avg";
}

function Subtract(...argsExp) {
    BinOperation.call(this, ...argsExp);
    this.createThisFunction = (...args) => new Subtract(...args);
    this.createCopyThisFunction = () => new Subtract(...argsExp);
    this.operation = (first, second) => first - second;
    this.getOperator = () => "-";
    this.myDiff = (diffVariable, firstExp, secondExp) => {
        return new Subtract(firstExp.diff(diffVariable), secondExp.diff(diffVariable));
    };
}

function Divide(...argsExp) {
    BinOperation.call(this, ...argsExp);
    this.createThisFunction = (...args) => new Divide(...args);
    this.createCopyThisFunction = () => new Divide(...argsExp);
    this.operation = (first, second) => first / second;
    this.getOperator = () => "/";
    this.myDiff = (diffVariable, firstExp, secondExp) => {
        return new Divide(
            new Subtract(new Multiply(firstExp.diff(diffVariable), secondExp),
                new Multiply(firstExp, secondExp.diff(diffVariable))),
            new Multiply(secondExp, secondExp));
    };
}

function Negate(exp) {
    UnaryOperation.call(this, exp);
    this.createCopyThisFunction = () => new Negate(exp);
    this.createThisFunction = (expression) => new Negate(expression);
    this.operation = (expression) => -expression;
    this.getOperator = () => "negate";
    this.diff = (diffVariable) => this.createThisFunction(this.exp.diff(diffVariable));
}


function Cosh(exp) {
    UnaryOperation.call(this, exp);
    this.createThisFunction = (expression) => new Cosh(expression);
    this.createCopyThisFunction = () => new Cosh(exp);
    this.operation = (expression) => Math.cosh(expression);
    this.getOperator = () => "cosh";
    this.diff = (diffVariable) => new Multiply(new Sinh(this.exp.createCopyThisFunction()), this.exp.diff(diffVariable));
}

function Sinh(exp) {
    UnaryOperation.call(this, exp);
    this.createThisFunction = (expression) => new Sinh(expression);
    this.createCopyThisFunction = () => new Sinh(exp);
    this.operation = (expression) => Math.sinh(expression);
    this.getOperator = () => " sinh";
    this.diff = (diffVariable) => new Multiply(new Cosh(this.exp.createCopyThisFunction()), this.exp.diff(diffVariable));
}


function parse(expression) {
    const tokens = expression.split(' ');
    let stack = [];
    for (let token of tokens) {
        switch (token) {
            case "x":
            case "y":
            case "z":
                stack.push(new Variable(token));
                break;
            case "+":
                const firstAddExp = stack.pop();
                stack.push(new Add(stack.pop(), firstAddExp));
                break;
            case "-":
                const firstSubtractExp = stack.pop();
                stack.push(new Subtract(stack.pop(), firstSubtractExp));
                break;
            case "*":
                const firstMultiplyExp = stack.pop();
                stack.push(new Multiply(stack.pop(), firstMultiplyExp));
                break;
            case "/":
                const firstDivideExp = stack.pop();
                stack.push(new Divide(stack.pop(), firstDivideExp));
                break;
            case "negate":
                const firstNegateExp = stack.pop();
                stack.push(new Negate(firstNegateExp));
                break;
            case "cosh":
                const firstCoshExp = stack.pop();
                stack.push(new Cosh(firstCoshExp));
                break;
            case "sinh":
                const firstSinhExp = stack.pop();
                stack.push(new Sinh(firstSinhExp));
                break;
            case "":
                break;
            default:
                if (!isNaN(token)) {
                    stack.push(new Const(parseInt(token)));
                }
        }
    }
    return stack.pop();
}

function mySplit(expression) {
    let tokens = [];
    let pos = 0;
    while (pos < expression.length) {
        if (expression[pos] === ' ') {
            pos++;
            continue;
        }
        if (expression[pos] === '(' || expression[pos] === ')') {
            tokens.push(expression[pos]);
            pos++;
            continue;
        }
        const start = pos;
        while (pos < expression.length && expression[pos] !== ' ' && expression[pos] !== ')' && expression[pos] !== "(") {
            pos++;
        }
        tokens.push(expression.slice(start, pos));
    }
    return tokens;
}

function isVariable(token) {
    return token === 'x' || token === 'y' || token === 'z';
}

function ParserError(message) {
    this.name = "ParserError";
    this.message = message;
}

ParserError.prototype = Error.prototype;

let nextToken;
let isNextClosingBracket;
let reverser;

function headParser(expression, myRatio) {
    ((tokens, myRatio) => {
        let pos = 0;
        nextToken = function next() {
            if (pos !== tokens.length) {
                return myRatio(tokens[pos++]);
            } else {
                return "EOF";
            }
        };
        isNextClosingBracket = function isNext() {
            return pos < tokens.length && tokens[pos] === myRatio(")");
        };

    })(reverser(mySplit(expression)), myRatio);
    let res = expParser();
    if (nextToken() !== "EOF") {
        throw new ParserError("extra characters met");
    }
    return res;
}


function expParser() {
    const token = nextToken();
    if (token === '(') {
        let res = opParser();
        if (nextToken() !== ")") {
            throw new ParserError("lost closing bracket");
        }
        return res;
    }
    if (isVariable(token)) {
        return new Variable(token);
    }
    if (isFinite(token)) {
        return new Const(parseInt(token));
    }
    if (token === "EOF") {
        throw new ParserError("missing expression");
    }
    throw new ParserError("Expected expression but met " + token);
}

function getArgsExp() {
    let argsExp = [];
    while (!isNextClosingBracket()) {
        argsExp.push(expParser());
    }
    return argsExp;
}

function opParser() {
    const token = nextToken();
    switch (token) {
        case "+":
            return new Add(...(reverser([expParser(), expParser()])));
            break;
        case "-":
            return new Subtract(...(reverser([expParser(), expParser()])));
            break;
        case "*":
            return new Multiply(...(reverser([expParser(), expParser()])));
            break;
        case "/":
            return new Divide(...(reverser([expParser(), expParser()])));
            break;
        case "negate":
            return new Negate(expParser());
            break;
        case "sum":
            return new Sum(...(reverser(getArgsExp())));
        case "avg":
            return new Avg(...(reverser(getArgsExp())));
        default:
            throw new ParserError("Expected operation but met " + token);
    }
}

function parsePrefix(expression) {
    reverser = (tokens) => tokens;
    return headParser(expression, (token) => token);
}

function parsePostfix(expression) {
    reverser = (tokens) => tokens.reverse();
    return headParser(expression, (token) => {
        switch (token) {
            case "(":
                return ")";
            case ")":
                return "(";
            default:
                return token;
        }
    });
}