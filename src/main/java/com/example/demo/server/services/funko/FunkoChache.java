package com.example.demo.server.services.funko;

import com.example.demo.common.models.Funko;
import com.example.demo.server.services.cache.Cache;

 interface FunkoChache extends Cache<Long, Funko> {
}
