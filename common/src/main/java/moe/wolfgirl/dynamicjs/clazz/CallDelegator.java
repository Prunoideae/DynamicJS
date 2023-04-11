package moe.wolfgirl.dynamicjs.clazz;

import dev.latvian.mods.rhino.Context;
import net.bytebuddy.dynamic.TargetType;
import net.bytebuddy.implementation.bind.annotation.*;

import java.util.function.Function;

public class CallDelegator<T> {

    private final Function<CallInfo<T>, Object> callback;
    private final Class<?> callbackType;
    private final Context context;

    public CallDelegator(Function<CallInfo<T>, Object> callback, Class<?> callbackType, Context context) {
        this.callback = callback;
        this.callbackType = callbackType;
        this.context = context;
    }


    @RuntimeType
    @SuppressWarnings("unchecked")
    public Object intercept(
            @RuntimeType @This Object self,
            @Super(strategy = Super.Instantiation.UNSAFE, proxyType = TargetType.class) Object parent,
            @AllArguments Object[] args
    ) {
        return Context.jsToJava(context, callback.apply(new CallInfo<>((T) self, parent, args)), callbackType);
    }

    public static final class CallInfo<T> {
        //TODO: support override<Q extends keyof T>(name: Q, callback: Internal.Function_<Internal.CallDelegator$CallInfo_<T, Parameters<NonNullable<T>[Q]>>, any>): this;
        private final T self;
        private final Object parent;
        private final Object[] args;

        public CallInfo(T self, Object parent, Object[] args) {
            this.self = self;
            this.args = args;
            this.parent = parent;
        }

        public T getSelf() {
            return self;
        }

        public Object[] getArgs() {
            return args;
        }

        public Object getParent() {
            return parent;
        }
    }
}
